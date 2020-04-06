package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.PromoCode;
import capstone.rt04.retailbackend.entities.Staff;
import capstone.rt04.retailbackend.entities.StaffLeave;
import capstone.rt04.retailbackend.repositories.LeaveRepository;
import capstone.rt04.retailbackend.repositories.StaffRepository;
import capstone.rt04.retailbackend.util.ErrorMessages;
import capstone.rt04.retailbackend.util.enums.LeaveStatusEnum;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.leave.StaffLeaveCannotDeleteException;
import capstone.rt04.retailbackend.util.exceptions.leave.StaffLeaveCannotUpdateException;
import capstone.rt04.retailbackend.util.exceptions.leave.StaffLeaveNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.promoCode.CreateNewPromoCodeException;
import capstone.rt04.retailbackend.util.exceptions.staff.StaffNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Date;

@Service
@Transactional
public class LeaveService {

    private final ValidationService validationService;
    private final StaffService staffService;
    private final LeaveRepository leaveRepository;
    private final StaffRepository staffRepository;


    public LeaveService(ValidationService validationService, StaffService staffService, LeaveRepository leaveRepository, StaffRepository staffRepository) {
        this.validationService = validationService;
        this.staffService = staffService;
        this.leaveRepository = leaveRepository;
        this.staffRepository = staffRepository;
    }

    public StaffLeave createNewLeave(StaffLeave staffLeave) throws InputDataValidationException, StaffNotFoundException {
        validationService.throwExceptionIfInvalidBean(staffLeave);
        Staff existingStaff = staffRepository.findById(staffLeave.getApplicant().getStaffId())
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + staffLeave.getApplicant().getStaffId() + " does not exist"));

        for(StaffLeave leave : existingStaff.getLeaves()){
            if((!leave.getStatus().equals(LeaveStatusEnum.REJECTED))&& (staffLeave.getFromDateTime().equals(leave.getFromDateTime()) || (staffLeave.getFromDateTime().after(staffLeave.getToDateTime())) ||
                    (staffLeave.getFromDateTime().after(leave.getFromDateTime()) && staffLeave.getFromDateTime().before(leave.getToDateTime())) ||
                staffLeave.getFromDateTime().equals(leave.getToDateTime())) ){
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("fromDateTime", ErrorMessages.OVERLAP_IN_LEAVE);
                throw new InputDataValidationException(errorMap, ErrorMessages.OVERLAP_IN_LEAVE);
            }
        }

        staffLeave.setStatus(LeaveStatusEnum.PENDING);
        StaffLeave savedLeave = leaveRepository.save(staffLeave);
        existingStaff.getLeaves().add(savedLeave);
        return  savedLeave;

    }

    public StaffLeave updateLeave(Long leaveId, Staff applicant, Date fromDate, Date toDate) throws InputDataValidationException, StaffNotFoundException, StaffLeaveCannotUpdateException, StaffLeaveNotFoundException {
        StaffLeave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new StaffLeaveNotFoundException("Leave with id: " + leaveId + " does not exist"));

        if(existingLeave.getStatus().equals(LeaveStatusEnum.APPROVED) || existingLeave.getStatus().equals(LeaveStatusEnum.ENDORSED)){
            throw new StaffLeaveCannotUpdateException("Leave has already been endorsed or approved");
        }

        Staff existingStaff = staffRepository.findById(applicant.getStaffId())
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + applicant.getStaffId() + " does not exist"));

        for(StaffLeave leave : existingStaff.getLeaves()){
            if(!leave.getStaffLeaveId().equals(leaveId) &&
                    ((!leave.getStatus().equals(LeaveStatusEnum.REJECTED))&&
                            ((fromDate.equals(leave.getFromDateTime()) ||
                    (fromDate.after(toDate)) ||
                    (fromDate.after(leave.getFromDateTime()) && fromDate.before(leave.getToDateTime())) ||
                    fromDate.equals(leave.getToDateTime())))) ){
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("fromDateTime", ErrorMessages.OVERLAP_IN_LEAVE);
                throw new InputDataValidationException(errorMap, ErrorMessages.OVERLAP_IN_LEAVE);
            }
        }

        existingLeave.setToDateTime(toDate);
        existingLeave.setFromDateTime(fromDate);
        return  existingLeave;

    }

    public List<StaffLeave> retrieveAllLeaves(Long staffId) throws StaffNotFoundException {
        Staff existingStaff = staffRepository.findById(staffId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + staffId + " does not exist"));
        return existingStaff.getLeaves();
    }

    public StaffLeave removeLeave(Long leaveId) throws StaffLeaveCannotDeleteException, StaffLeaveNotFoundException, StaffNotFoundException {
        StaffLeave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new StaffLeaveNotFoundException("Leave with id: " + leaveId + " does not exist"));

        if(existingLeave.getStatus().equals(LeaveStatusEnum.APPROVED) || existingLeave.getStatus().equals(LeaveStatusEnum.ENDORSED)){
            throw new StaffLeaveCannotDeleteException("Leave has already been endorsed or approved");
        }
        Staff staff = staffRepository.findById(existingLeave.getApplicant().getStaffId())
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + existingLeave.getApplicant().getStaffId() + " does not exist"));
        staff.getLeaves().remove(existingLeave);
        leaveRepository.delete(existingLeave);
        return existingLeave;
    }

    public List<StaffLeave> retrieveAllLeavesManager(Long staffId) throws StaffNotFoundException {
        Staff manager = staffRepository.findById(staffId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + staffId + " does not exist"));

        List<Staff> staffList = new ArrayList<Staff>();
        if(manager.getDepartment().getDepartmentName().equals("Store")){
            staffList = staffRepository.findAllByStore_StoreId(manager.getStore().getStoreId());
        } else {
            staffList = staffRepository.findByDepartment(manager.getDepartment());
        }

        List<StaffLeave> leaves = new ArrayList<StaffLeave>();
        for(Staff staff : staffList){
            for(StaffLeave leave : staff.getLeaves()){
                leaves.add(leave);
            }
        }

        return leaves;
    }

    public List<StaffLeave> retrieveAllPendingLeaves(Long staffId) throws StaffNotFoundException {
        Staff manager = staffRepository.findById(staffId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + staffId + " does not exist"));

        List<Staff> staffList = new ArrayList<Staff>();
        if(manager.getDepartment().getDepartmentName().equals("Store")){
            staffList = staffRepository.findAllByStore_StoreId(manager.getStore().getStoreId());
        } else {
            staffList = staffRepository.findByDepartment(manager.getDepartment());
        }

        List<StaffLeave> leaves = new ArrayList<StaffLeave>();
        for(Staff staff : staffList){
            for(StaffLeave leave : staff.getLeaves()){
                if(leave.getStatus().equals(LeaveStatusEnum.PENDING)) {
                    leaves.add(leave);
                }
            }
        }

        return leaves;
    }

    public List<StaffLeave> retrieveAllLeavesHR(){
        List<StaffLeave> allLeaves = (List<StaffLeave>) leaveRepository.findAll();
        List<StaffLeave> leaves = new ArrayList<StaffLeave>();

        for(StaffLeave leave : allLeaves){
            if(leave.getStatus().equals(LeaveStatusEnum.APPROVED)){
                leaves.add(leave);
            } if(leave.getRejectedBy() != null){
                if(leave.getRejectedBy().getDepartment().getDepartmentName().equals("HR")){
                    leaves.add(leave);
                }
            }
        }
        return leaves;
    }

    public List<StaffLeave> retrieveAllEndorsedLeaves(){
        List<StaffLeave> allLeaves = (List<StaffLeave>) leaveRepository.findAll();
        List<StaffLeave> leaves = new ArrayList<StaffLeave>();

        for(StaffLeave leave : allLeaves){
            if(leave.getStatus().equals(LeaveStatusEnum.ENDORSED)){
                leaves.add(leave);
            }
        }
        return leaves;
    }

    public StaffLeave endorseRejectLeave (Long leaveId, Long managerId, Boolean action) throws StaffNotFoundException, StaffLeaveNotFoundException {
        Staff manager = staffRepository.findById(managerId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + managerId + " does not exist"));

        StaffLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new StaffLeaveNotFoundException("Leave with id: " + leaveId + " does not exist"));

        if(action == true){
            leave.setStatus(LeaveStatusEnum.ENDORSED);
            leave.setEndorser(manager);
        } else{
            leave.setStatus(LeaveStatusEnum.REJECTED);
            leave.setRejectedBy(manager);
        }

        return leave;
    }

    public StaffLeave approveRejectLeave (Long leaveId, Long hrId, Boolean action) throws StaffNotFoundException, StaffLeaveNotFoundException {
        Staff hr = staffRepository.findById(hrId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + hrId + " does not exist"));

        StaffLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new StaffLeaveNotFoundException("Leave with id: " + leaveId + " does not exist"));

        if(action == true){
            leave.setStatus(LeaveStatusEnum.APPROVED);
            leave.setApprover(hr);
        } else{
            leave.setStatus(LeaveStatusEnum.REJECTED);
            leave.setRejectedBy(hr);
        }

        return leave;
    }
}
