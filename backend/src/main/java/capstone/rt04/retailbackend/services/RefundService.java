package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.repositories.RefundLineItemHandlerRepository;
import capstone.rt04.retailbackend.repositories.RefundLineItemRepository;
import capstone.rt04.retailbackend.repositories.RefundRepository;
import capstone.rt04.retailbackend.request.refund.RefundLineItemRequest;
import capstone.rt04.retailbackend.request.refund.RefundRequest;
import capstone.rt04.retailbackend.request.refund.UpdateRefundLineItemHandlerRequest;
import capstone.rt04.retailbackend.util.ErrorMessages;
import capstone.rt04.retailbackend.util.enums.RefundModeEnum;
import capstone.rt04.retailbackend.util.enums.RefundProgressEnum;
import capstone.rt04.retailbackend.util.enums.RefundStatusEnum;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.customer.CustomerNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.promoCode.PromoCodeNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.refund.RefundNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.store.StoreNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.transaction.TransactionNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.warehouse.WarehouseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RefundService {
    private final ValidationService validationService;
    private final ProductService productService;
    private final TransactionService transactionService;
    private final CustomerService customerService;
    private final PromoCodeService promoCodeService;
    private final RefundRepository refundRepository;
    private final RefundLineItemRepository refundLineItemRepository;
    private final RefundLineItemHandlerRepository refundLineItemHandlerRepository;
    private final StoreService storeService;
    private final WarehouseService warehouseService;

    public RefundService(ValidationService validationService,
                         ProductService productService,
                         RefundRepository refundRepository,
                         RefundLineItemRepository refundLineItemRepository,
                         RefundLineItemHandlerRepository refundLineItemHandlerRepository,
                         TransactionService transactionService,
                         CustomerService customerService,
                         PromoCodeService promoCodeService,
                         StoreService storeService,
                         WarehouseService warehouseService) {
        this.validationService = validationService;
        this.productService = productService;
        this.refundRepository = refundRepository;
        this.refundLineItemRepository = refundLineItemRepository;
        this.transactionService = transactionService;
        this.refundLineItemHandlerRepository = refundLineItemHandlerRepository;
        this.customerService = customerService;
        this.promoCodeService = promoCodeService;
        this.storeService = storeService;
        this.warehouseService = warehouseService;
    }

    public Refund createRefund(RefundRequest refundRequest) throws TransactionNotFoundException, CustomerNotFoundException, InputDataValidationException, PromoCodeNotFoundException, RefundNotFoundException, StoreNotFoundException, WarehouseNotFoundException {
        List<RefundLineItem> refundLineItemList = new ArrayList<>();
        List<RefundLineItemHandler> refundLineItemHandlers = new ArrayList<>();
        Map<String, String> errorMap = new HashMap<>();
        Integer totalQuantity = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;
        Customer customer = customerService.retrieveCustomerByCustomerId(refundRequest.getCustomerId());
        Long promoCodeId = Long.valueOf("0");
        boolean isRefundedBefore = false;

        if (refundRequest.getReason() == null || refundRequest.getReason().isEmpty()) {
            errorMap.put("reason", ErrorMessages.REFUND_REASON_EMPTY);
            throw new InputDataValidationException(errorMap, ErrorMessages.REFUND_REASON_EMPTY);
        }
        if (refundRequest.getRefundMode() == null || refundRequest.getRefundMode().isEmpty()) {
            errorMap.put("reason", ErrorMessages.REFUND_REASON_EMPTY);
            throw new InputDataValidationException(errorMap, ErrorMessages.REFUND_REASON_EMPTY);
        }
        if (refundRequest.getRefundMode().equals("IN_STORE") && refundRequest.getStoreId() == null) {
            errorMap.put("store", ErrorMessages.REFUND_STORE_ID_EMPTY);
            throw new InputDataValidationException(errorMap, ErrorMessages.REFUND_STORE_ID_EMPTY);
        }
        for (RefundLineItemRequest refundLineItemRequest : refundRequest.getRefundLineItemRequests()) {
            TransactionLineItem transactionLineItem = transactionService.retrieveTransactionLineItemById(refundLineItemRequest.getTransactionLineItemId());
            BigDecimal unitPrice;
            if(transactionLineItem.getRefundLineItems().size() > 0) {
                isRefundedBefore = true;
            }
            if (transactionLineItem.getFinalSubTotal() != null) {
                unitPrice = transactionLineItem.getFinalSubTotal().divide(new BigDecimal(transactionLineItem.getQuantity()));
            } else {
                unitPrice = transactionLineItem.getInitialSubTotal().divide(new BigDecimal(transactionLineItem.getQuantity()));
            }
            if(transactionLineItem.getTransaction().getPromoCode() != null) {
                promoCodeId = transactionLineItem.getTransaction().getPromoCode().getPromoCodeId();
            }
            Integer quantityToRefund = refundLineItemRequest.getQuantityToRefund();
            BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantityToRefund));
            totalQuantity += quantityToRefund;
            refundAmount = refundAmount.add(totalPrice);

            RefundLineItemHandler refundLineItemHandler;
            if(refundRequest.getStoreId() == null) {
                refundLineItemHandler = new RefundLineItemHandler(refundLineItemRequest.getStaffId(), quantityToRefund, RefundProgressEnum.PENDING_DELIVERY);
            } else {
                refundLineItemHandler = new RefundLineItemHandler(refundLineItemRequest.getStaffId(), quantityToRefund, RefundProgressEnum.HANDLED_BY_STAFF);
            }

            refundLineItemHandlers.add(refundLineItemHandler);

            RefundLineItem refundLineItem = new RefundLineItem(unitPrice, quantityToRefund, totalPrice);
            refundLineItem.setTransactionLineItem(transactionLineItem);
            refundLineItem.getRefundLineItemHandlerList().add(refundLineItemHandler);

            refundLineItemList.add(refundLineItem);
            refundLineItemHandler.setRefundLineItem(refundLineItem);
        }

        PromoCode promoCode;
        if(promoCodeId != Long.valueOf("0")) {
            promoCode = promoCodeService.retrievePromoCodeById(promoCodeId);
            if(!(promoCode.getFlatDiscount() == null) && !isRefundedBefore){
                refundAmount = refundAmount.subtract(promoCode.getFlatDiscount());
            } else if (!(promoCode.getPercentageDiscount() == null)) {
                BigDecimal val = promoCode.getPercentageDiscount().multiply(BigDecimal.valueOf(0.01));
                val = BigDecimal.ONE.subtract(val);
                refundAmount = refundAmount.multiply(val);
            }
        }



        // Create Refund
        Refund refund;
        if(refundRequest.getStoreId() == null) {
            refund = new Refund(totalQuantity, refundAmount, RefundModeEnum.valueOf(refundRequest.getRefundMode()), RefundStatusEnum.PENDING, refundRequest.getReason(), isRefundedBefore);
        } else {
            refund = new Refund(totalQuantity, refundAmount, RefundModeEnum.valueOf(refundRequest.getRefundMode()), RefundStatusEnum.PROCESSING, refundRequest.getReason(), isRefundedBefore);
        }


        refund.setCustomer(customer);
        customer.getRefunds().add(refund);
        refund.setRefundLineItems(refundLineItemList);
        for (RefundLineItem rle : refundLineItemList) {
            rle.setRefund(refund);
        }

        for (RefundLineItemHandler refundLineItemHandler : refundLineItemHandlers) {
            errorMap = validationService.generateErrorMap(refundLineItemHandler);
            if (errorMap == null) {
                refundLineItemHandlerRepository.save(refundLineItemHandler);
            }
        }

        for (RefundLineItem refundLineItem : refundLineItemList) {
            errorMap = validationService.generateErrorMap(refundLineItem);
            if (errorMap == null) {
                refundLineItemRepository.save(refundLineItem);
            }
        }



        refundRepository.save(refund);
        if(refundRequest.getStoreId() != null) {
            Store store = storeService.retrieveStoreById(refundRequest.getStoreId());
            refund.setStore(store);
        } else {
            List<Warehouse> warehouses = warehouseService.retrieveAllWarehouses();
            Warehouse warehouse = warehouseService.retrieveWarehouseById(warehouses.get(0).getWarehouseId());
            refund.setWarehouse(warehouse);
        }
        if(refundRequest.getStoreId() != null) {
            refund = updateInStoreRefund(refundRequest.getCustomerId(), refundLineItemList);
        }

        return refund;
    }

    public Refund updateInStoreRefund(Long staffId, List<RefundLineItem> refundLineItemList) throws RefundNotFoundException {
//        private Long refundLineItemId;
//        private String refundProgressEnum;
//        private Long staffId;
//        private Integer quantityConfirmedRefunded;
        Refund refund = null;
        for(RefundLineItem rli : refundLineItemList) {
            RefundLineItem refundLineItem = retrieveRefundLineItemById(rli.getRefundLineItemId());
            RefundLineItemHandler refundLineItemHandler = new RefundLineItemHandler(staffId,
                    refundLineItem.getQuantity(),
                    RefundProgressEnum.REFUND_SUCCESS);
            refundLineItemHandler.setRefundLineItem(refundLineItem);
            refundLineItemHandlerRepository.save(refundLineItemHandler);
            refundLineItem.getRefundLineItemHandlerList().add(refundLineItemHandler);

            refund = retrieveRefundById(refundLineItem.getRefund().getRefundId());
            refund.setRefundStatus(RefundStatusEnum.COMPLETED);
        }
        return refund;
    }

    // Update multiple RefundLineItem
    public Refund updateRefundLineItemsStatus(List<UpdateRefundLineItemHandlerRequest> updateRefundLineItemHandlerRequests) throws RefundNotFoundException {
        Refund refund = null;
        int totalQuantity = 0;
        boolean hasRejected = false;
        int totalRejectedQuantity = 0;
        if (updateRefundLineItemHandlerRequests.size() == 0) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundLineItemHandlerRequests", ErrorMessages.REFUND_NOT_SELECTED);
            throw new RefundNotFoundException(errorMap, "Refund Items must be selected");
        }

        for (UpdateRefundLineItemHandlerRequest updateRefundLineItemHandlerRequest : updateRefundLineItemHandlerRequests) {
            RefundLineItemHandler refundLineItemHandlerCheck = retrieveRefundLineItemHandlersByRefundLineItemIdAndByTimestamp(updateRefundLineItemHandlerRequest.getRefundLineItemId());

            //if refund success, want to change back to refund in progress, cannot do so
            if (refundLineItemHandlerCheck.getRefundProgressEnum().getValue() > RefundProgressEnum.valueOf(updateRefundLineItemHandlerRequest.getRefundProgressEnum()).getValue()) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("refundLineItemHandlerId", ErrorMessages.REFUND_LINE_ITEM_HANDLER_ID_REQUIRED);
                throw new RefundNotFoundException(errorMap, "Cannot Set Refund Status backwards");
            }
            if (updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals("REFUND_SUCCESS")
                    || updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals("REFUND_REJECTED")) {
                if (updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals("REFUND_REJECTED")) {
                    hasRejected = true;
                    totalRejectedQuantity += updateRefundLineItemHandlerRequest.getQuantityConfirmedRefunded();
                }
                totalQuantity += updateRefundLineItemHandlerRequest.getQuantityConfirmedRefunded();
            }
            RefundLineItem refundLineItem = retrieveRefundLineItemById(updateRefundLineItemHandlerRequest.getRefundLineItemId());
            RefundLineItemHandler refundLineItemHandler = new RefundLineItemHandler(updateRefundLineItemHandlerRequest.getStaffId(),
                    updateRefundLineItemHandlerRequest.getQuantityConfirmedRefunded(),
                    RefundProgressEnum.valueOf(updateRefundLineItemHandlerRequest.getRefundProgressEnum()));
            refundLineItemHandler.setRefundLineItem(refundLineItem);
            refundLineItemHandlerRepository.save(refundLineItemHandler);
            refundLineItem.getRefundLineItemHandlerList().add(refundLineItemHandler);

            refund = retrieveRefundById(refundLineItem.getRefund().getRefundId());
            if(updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals(RefundProgressEnum.RECEIVED_BY_STORE.toString())) {
                refund.setRefundStatus(RefundStatusEnum.PROCESSING);
            } else if(updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals(RefundProgressEnum.HANDLED_BY_STAFF.toString())) {
                refund.setRefundStatus(RefundStatusEnum.PROCESSING);
            }
        }

        Refund refundFinal = retrieveRefundById(refund.getRefundId());

        if (refundFinal.getQuantity() == totalRejectedQuantity) {
            refundFinal.setRefundStatus(RefundStatusEnum.REJECTED);
        } else if (refundFinal.getQuantity() == totalQuantity) {
            if (hasRejected) {
                refundFinal.setRefundStatus(RefundStatusEnum.COMPLETED_WITH_REJECTED_PRODUCTS);
            } else {
                refundFinal.setRefundStatus(RefundStatusEnum.COMPLETED);
            }
        } else if (totalQuantity > 0) {
            refundFinal.setRefundStatus(RefundStatusEnum.PARTIALLY_COMPLETE);
        }

        return refundFinal;
    }


    public RefundLineItemHandler retrieveRefundLineItemHandlersByRefundLineItemIdAndByTimestamp(Long refundLineItemId) {
        List<RefundLineItemHandler> refundLineItemHandlers = refundLineItemHandlerRepository.findByRefundLineItemIdAndHandledDateTime(refundLineItemId);
        if (refundLineItemHandlers.size() > 0) {
            return refundLineItemHandlers.get(0);
        } else {
            return null;
        }
    }

    // Modify multiple Refunds
    public List<Refund> updateRefundsStatus(List<Refund> refunds, Long staffId, Integer quantityConfirmedRefunded, String refundProgressEnum) {
        for (Refund refund : refunds) {
            for (RefundLineItem refundLineItem : refund.getRefundLineItems()) {
                RefundLineItemHandler refundLineItemHandler = createRefundLineItemHandlerForRefundLineItem(staffId, quantityConfirmedRefunded, refundProgressEnum);
                refundLineItemHandler.setRefundLineItem(refundLineItem);
                refundLineItemHandlerRepository.save(refundLineItemHandler);

                refundLineItem.getRefundLineItemHandlerList().add(refundLineItemHandler);
            }
        }
        return refunds;
    }

    public RefundLineItemHandler createRefundLineItemHandlerForRefundLineItem(Long staffId, Integer quantityConfirmedRefunded, String refundProgressEnum) {
        return new RefundLineItemHandler(staffId, quantityConfirmedRefunded, RefundProgressEnum.valueOf(refundProgressEnum));
    }

    public RefundLineItem retrieveRefundLineItemById(Long refundLineItemId) throws RefundNotFoundException {
        if (refundLineItemId == null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundLineItemId", ErrorMessages.REFUND_LINE_ITEM_ID_REQUIRED);
            throw new RefundNotFoundException(errorMap, "Refund Line Item ID not provided");
        }
        RefundLineItem refundLineItem = refundLineItemRepository.findById(refundLineItemId)
                .orElseThrow(() -> new RefundNotFoundException("Refund Line Item ID " + refundLineItemId + " does not exist!"));

        return refundLineItem;
    }

    public RefundLineItemHandler retrieveRefundLineItemHandlerById(Long refundLineItemHandlerId) throws RefundNotFoundException {
        if (refundLineItemHandlerId == null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundLineItemHandlerId", ErrorMessages.REFUND_LINE_ITEM_HANDLER_ID_REQUIRED);
            throw new RefundNotFoundException(errorMap, "Refund Line Item Handler ID not provided");
        }
        RefundLineItemHandler refundLineItemHandler = refundLineItemHandlerRepository.findById(refundLineItemHandlerId)
                .orElseThrow(() -> new RefundNotFoundException("Refund Line Item ID " + refundLineItemHandlerId + " does not exist!"));

        return refundLineItemHandler;
    }

    public Refund retrieveRefundById(Long refundId) throws RefundNotFoundException {
        if (refundId == null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundId", ErrorMessages.REFUND_ID_REQUIRED);
            throw new RefundNotFoundException(errorMap, "Refund ID not provided");
        }
        Refund refund = refundRepository.findByRefundId(refundId)
                .orElseThrow(() -> new RefundNotFoundException("Refund ID " + refundId + " does not exist!"));

        return refund;
    }

    public List<Refund> retrieveAllRefunds() {
        List<Refund> refunds = refundRepository.findAll();
        lazilyLoadRefunds(refunds);

        return refunds;
    }

    public List<Refund> retrieveAllRefundsByParameter(Long storeId, Long warehouseId) {
        List<Refund> refunds = retrieveAllRefunds();
        List<Refund> refundsByParameter = new ArrayList<>();
        if(storeId != null) {
            for(Refund r: refunds) {
                if(r.getRefundMode()== RefundModeEnum.IN_STORE && r.getStore().getStoreId().equals(storeId)) {
                    refundsByParameter.add(r);
                }
            }
        } else {
            for(Refund r: refunds) {
                if(r.getRefundMode() == RefundModeEnum.ONLINE) {
                    refundsByParameter.add(r);
                }
            }
        }
        lazilyLoadRefunds(refundsByParameter);
        return refundsByParameter;
    }

    public List<Refund> retrieveRefundsByCustomerId(Long customerId) {
        List<Refund> refunds = refundRepository.findAllByCustomer_CustomerId(customerId);
        lazilyLoadRefunds(refunds);
        return refunds;
    }

    public List<Refund> retrieveRefundByTransactionId(Long transactionId) throws RefundNotFoundException {
        List<Refund> refunds = refundRepository.findByTransactionId(transactionId);
        if(refunds.isEmpty() || refunds.size() == 0) {
            throw new RefundNotFoundException("Refund with Transaction ID " + transactionId + " does not exist!");
        }
        return refunds;
    }

    public void lazilyLoadRefunds(List<Refund> refunds) {
        for (Refund refund : refunds) {
            refund.getCustomer();
            refund.getRefundLineItems().size();
            for (RefundLineItem refundLineItem : refund.getRefundLineItems()) {
                refundLineItem.getRefundLineItemHandlerList().size();
            }
        }
    }


}
