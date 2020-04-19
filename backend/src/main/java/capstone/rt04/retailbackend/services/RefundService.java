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
import capstone.rt04.retailbackend.util.exceptions.staff.StaffNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.store.StoreNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.transaction.TransactionNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.warehouse.WarehouseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final StaffService staffService;

    public RefundService(ValidationService validationService,
                         ProductService productService,
                         RefundRepository refundRepository,
                         RefundLineItemRepository refundLineItemRepository,
                         RefundLineItemHandlerRepository refundLineItemHandlerRepository,
                         TransactionService transactionService,
                         CustomerService customerService,
                         PromoCodeService promoCodeService,
                         StoreService storeService,
                         WarehouseService warehouseService,
                         StaffService staffService) {
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
        this.staffService = staffService;
    }

    public Refund createRefund(RefundRequest refundRequest) throws TransactionNotFoundException, CustomerNotFoundException, InputDataValidationException, PromoCodeNotFoundException, RefundNotFoundException, StoreNotFoundException, WarehouseNotFoundException, StaffNotFoundException {
        List<RefundLineItem> refundLineItemList = new ArrayList<>();
        List<RefundLineItemHandler> refundLineItemHandlers = new ArrayList<>();
        Map<String, String> errorMap = new HashMap<>();
        Integer totalQuantity = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;
        Customer customer = customerService.retrieveCustomerByCustomerId(refundRequest.getCustomerId());
        Staff staff = null;
        if(refundRequest.getRefundLineItemRequests().size()>0 ) {
            if(refundRequest.getRefundLineItemRequests().get(0).getStaffId() != null) {
                staff = staffService.retrieveStaffByStaffId(refundRequest.getRefundLineItemRequests().get(0).getStaffId());
            }
        }
        Long promoCodeId = Long.valueOf("0");
        boolean isRefundedBefore = false;

        int qty = 0;
        for(RefundLineItemRequest refundLineItemRequest : refundRequest.getRefundLineItemRequests()) {
            qty += refundLineItemRequest.getQuantityToRefund();
        }

        if(qty == 0) {
            errorMap.put("quantity", "Refund quantity must not be empty");
            throw new InputDataValidationException(errorMap, "Refund quantity must not be empty");
        }

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
        Transaction currTransaction = null;
        for (RefundLineItemRequest refundLineItemRequest : refundRequest.getRefundLineItemRequests()) {

            if(refundLineItemRequest.getQuantityToRefund() == 0) {
                continue;
            }

            TransactionLineItem transactionLineItem = transactionService.retrieveTransactionLineItemById(refundLineItemRequest.getTransactionLineItemId());
            currTransaction = transactionLineItem.getTransaction();
            BigDecimal unitPrice;
            if (transactionLineItem.getFinalSubTotal() != null) {
                unitPrice = transactionLineItem.getFinalSubTotal().divide(new BigDecimal(transactionLineItem.getQuantity()), 3, RoundingMode.HALF_UP);
            } else {
                unitPrice = transactionLineItem.getInitialSubTotal().divide(new BigDecimal(transactionLineItem.getQuantity()), 3, RoundingMode.HALF_UP);
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
            if(!(promoCode.getFlatDiscount() == null)){
//                const amountBeforeFlatDisc = (beforeAmt/currTransaction.finalTotalPrice)*inputState.promoCode.flatDiscount;
//                amt -= amountBeforeFlatDisc;
                BigDecimal promoRefund = refundAmount;
                promoRefund = promoRefund.divide(currTransaction.getInitialTotalPrice(),3,RoundingMode.HALF_UP);
                promoRefund = promoRefund.multiply(promoCode.getFlatDiscount());
                refundAmount = refundAmount.subtract(promoRefund);
//                refundAmount = refundAmount.subtract(promoCode.getFlatDiscount());
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
        if(refundRequest.getStoreId() != null && staff.getStaffId()!= null) {
            refund = updateInStoreRefund(staff.getStaffId(), refundLineItemList);
        }

        return refund;
    }

    public Refund updateInStoreRefund(Long staffId, List<RefundLineItem> refundLineItemList) throws RefundNotFoundException, StaffNotFoundException, WarehouseNotFoundException {
//        private Long refundLineItemId;
//        private String refundProgressEnum;
//        private Long staffId;
//        private Integer quantityConfirmedRefunded;
        Refund refund = null;
        Staff staff = staffService.retrieveStaffByStaffId(staffId);
        List<Warehouse> warehouses = warehouseService.retrieveAllWarehouses();
        Warehouse warehouse = warehouseService.retrieveWarehouseById(warehouses.get(0).getWarehouseId());
        for(RefundLineItem rli : refundLineItemList) {
            RefundLineItem refundLineItem = retrieveRefundLineItemById(rli.getRefundLineItemId());
            RefundLineItemHandler refundLineItemHandler = new RefundLineItemHandler(staffId,
                    refundLineItem.getQuantity(),
                    RefundProgressEnum.REFUND_SUCCESS);
            List<ProductStock> productStocks = refundLineItem.getTransactionLineItem().getProductVariant().getProductStocks();
            for(ProductStock ps: productStocks) {
                if(staff.getStore() != null && ps.getStore() != null && ps.getStore().getStoreId().equals(staff.getStore().getStoreId())) {
                    ps.setQuantity(refundLineItem.getQuantity()+ ps.getQuantity());
                }
            }
            refundLineItemHandler.setRefundLineItem(refundLineItem);
            refundLineItemHandlerRepository.save(refundLineItemHandler);
            refundLineItem.getRefundLineItemHandlerList().add(refundLineItemHandler);

            refund = retrieveRefundById(refundLineItem.getRefund().getRefundId());
            refund.setRefundStatus(RefundStatusEnum.COMPLETED);
        }
        return refund;
    }

    // Update multiple RefundLineItem
    public Refund updateRefundLineItemsStatus(List<UpdateRefundLineItemHandlerRequest> updateRefundLineItemHandlerRequests) throws RefundNotFoundException, StaffNotFoundException, WarehouseNotFoundException {
        Refund refund = null;
        int totalQuantity = 0;
        boolean hasRejected = false;
        int totalRejectedQuantity = 0;
        Staff staff = staffService.retrieveStaffByStaffId(updateRefundLineItemHandlerRequests.get(0).getStaffId());
        List<Warehouse> warehouses = warehouseService.retrieveAllWarehouses();
        Warehouse warehouse = warehouseService.retrieveWarehouseById(warehouses.get(0).getWarehouseId());
        if (updateRefundLineItemHandlerRequests.size() == 0) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundLineItemHandlerRequests", ErrorMessages.REFUND_NOT_SELECTED);
            throw new RefundNotFoundException(errorMap, "Refund Items must be selected");
        }
        boolean canSubmit = false;
        for (UpdateRefundLineItemHandlerRequest updateRefundLineItemHandlerRequest : updateRefundLineItemHandlerRequests) {
            RefundLineItemHandler refundLineItemHandlerCheck = retrieveRefundLineItemHandlersByRefundLineItemIdAndByTimestamp(updateRefundLineItemHandlerRequest.getRefundLineItemId());

            if (refundLineItemHandlerCheck.getRefundProgressEnum().getValue() < RefundProgressEnum.valueOf(updateRefundLineItemHandlerRequest.getRefundProgressEnum()).getValue()) {
                canSubmit = true;
            }

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

            if(updateRefundLineItemHandlerRequest.getRefundProgressEnum().equals("REFUND_SUCCESS")) {
                List<ProductStock> productStocks = refundLineItem.getTransactionLineItem().getProductVariant().getProductStocks();
                for(ProductStock ps: productStocks) {
                    if(staff.getStore() != null && ps.getStore() != null && ps.getStore().getStoreId().equals(staff.getStore().getStoreId())) {
                        ps.setQuantity(updateRefundLineItemHandlerRequest.getQuantityConfirmedRefunded()+ ps.getQuantity());
                    } else if (ps.getWarehouse() != null && warehouse.getWarehouseId().equals(ps.getWarehouse().getWarehouseId())) {
                        ps.setQuantity(updateRefundLineItemHandlerRequest.getQuantityConfirmedRefunded()+ ps.getQuantity());
                    }
                }
            }

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
        if (!canSubmit) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("refundLineItemHandlerId", ErrorMessages.REFUND_LINE_ITEM_HANDLER_ID_REQUIRED);
            throw new RefundNotFoundException(errorMap, "Refund cannot be updated");
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
