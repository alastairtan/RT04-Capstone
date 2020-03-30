package capstone.rt04.retailbackend.controllers;

import capstone.rt04.retailbackend.entities.Transaction;
import capstone.rt04.retailbackend.request.transaction.TransactionRetrieveRequest;
import capstone.rt04.retailbackend.response.GenericErrorResponse;
import capstone.rt04.retailbackend.services.RelationshipService;
import capstone.rt04.retailbackend.services.TransactionService;
import capstone.rt04.retailbackend.util.exceptions.transaction.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static capstone.rt04.retailbackend.util.routeconstants.TransactionControllerRoutes.*;


@RestController
@RequestMapping(TRANSACTION_BASE_ROUTE)
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class TransactionController {

    private final TransactionService transactionService;
    private final RelationshipService relationshipService;

    public TransactionController(TransactionService transactionService, RelationshipService relationshipService) {
        this.transactionService = transactionService;
        this.relationshipService = relationshipService;
    }

//    @PostMapping(CREATE_TRANSACTION)
//    public ResponseEntity<?> createNewTransaction(@RequestBody TransactionCreateRequest transactionCreateRequest) throws CustomerNotFoundException, InvalidCartTypeException {
//        Customer customer = transactionService.createNewTransaction(transactionCreateRequest.getCustomerId(),
//                transactionCreateRequest.getShoppingCartId(), transactionCreateRequest.getCartType());
//        relationshipService.clearCustomerRelationships(customer);
//        return new ResponseEntity<>(customer, HttpStatus.CREATED);
//    }

    @GetMapping(RETRIEVE_TRANSACTION_BY_ID)
    public ResponseEntity<?> retrieveTransactionById(@PathVariable Long transactionId) {
        try {
            Transaction transaction = transactionService.retrieveTransactionById(transactionId);
            relationshipService.clearTransactionRelationships(transaction);
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } catch (TransactionNotFoundException ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(RETRIEVE_TRANSACTION_BY_ORDER_NUMBER)
    public ResponseEntity<?> retrieveTransactionByOrderNumber(@PathVariable String orderNumber) {
        try {
            Transaction transaction = transactionService.retrieveTransactionByOrderNumber(orderNumber);
            relationshipService.clearTransactionRelationshipsForStaffSide(transaction);
            relationshipService.clearCustomerRelationships(transaction.getCustomer());
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } catch (TransactionNotFoundException ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(RETRIEVE_INSTORE_COLLECTION_TRANSACTION)
    public ResponseEntity<?> retrieveInstoreCollectionTransaction() {
        List<Transaction> transactions = transactionService.retrieveInstoreCollectionTransaction();
        for (Transaction txn : transactions) {
            relationshipService.clearTransactionRelationships(txn);
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping(RETRIEVE_ALL_TRANSACTIONS)
    public ResponseEntity<?> retrievePastOrders() {
        List<Transaction> txns = transactionService.retrievePastOrders();
        for (Transaction txn : txns) {
            relationshipService.clearTransactionRelationships(txn);
        }
        return new ResponseEntity<>(txns, HttpStatus.OK);
    }

    @GetMapping(RETRIEVE_CUSTOMER_TRANSACTIONS)
    public ResponseEntity<?> retrieveCustomerTransactions(@RequestParam Long customerId) {
        List<Transaction> txns = transactionService.retrieveCustomerTransactions(customerId);
        for (Transaction txn : txns) {
            relationshipService.clearTransactionRelationships(txn);
        }
        Collections.sort(txns, Comparator.comparing(Transaction::getTransactionId).reversed());
        return new ResponseEntity<>(txns, HttpStatus.OK);
    }

    @PostMapping(RETRIEVE_MATCHED_TRANSACTIONS)
    public ResponseEntity<?> retrieveMatchedOrders(@RequestBody TransactionRetrieveRequest transactionRetrieveRequest) {
        try {
            List<Transaction> transactions = transactionService.filterSortOrderHistory(transactionRetrieveRequest.getCustomerId(), transactionRetrieveRequest.getCollectionMode(), transactionRetrieveRequest.getDeliveryStatus(),
                    transactionRetrieveRequest.getStartDate(), transactionRetrieveRequest.getEndDate(),
                    transactionRetrieveRequest.getSortEnum());
            for (Transaction transaction : transactions) {
                relationshipService.clearTransactionRelationships(transaction);
            }
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
