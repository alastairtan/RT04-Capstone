package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.repositories.*;
import capstone.rt04.retailbackend.util.ErrorMessages;
import capstone.rt04.retailbackend.util.enums.RoleNameEnum;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.customer.CustomerNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.staff.*;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class StaffService {
    private JavaMailSender javaMailSender;

    private final Environment environment;

    private final ValidationService validationService;

    private final StaffRepository staffRepository;
    private final AddressRepository addressRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AdvertisementRepository advertisementRepository;
    private final DeliveryRepository deliveryRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffLeaveRepository staffLeaveRepository;
    private final PayrollRepository payrollRepository;
    private final ReviewRepository reviewRepository;
    private final RoleRepository roleRepository;
    private final RosterRepository rosterRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();




    public StaffService(JavaMailSender javaMailSender, Environment environment, ValidationService validationService, StaffRepository staffRepository, AddressRepository addressRepository, VerificationCodeRepository verificationCodeRepository, AdvertisementRepository advertisementRepository, DeliveryRepository deliveryRepository, DepartmentRepository departmentRepository, StaffLeaveRepository staffLeaveRepository, ReviewRepository reviewRepository, PayrollRepository payrollRepository, RoleRepository roleRepository, RosterRepository rosterRepository) {
        this.javaMailSender = javaMailSender;
        this.environment = environment;
        this.validationService = validationService;
        this.staffRepository = staffRepository;
        this.addressRepository = addressRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.advertisementRepository = advertisementRepository;
        this.deliveryRepository = deliveryRepository;
        this.departmentRepository = departmentRepository;
        this.staffLeaveRepository = staffLeaveRepository;
        this.reviewRepository = reviewRepository;
        this.payrollRepository = payrollRepository;
        this.roleRepository = roleRepository;
        this.rosterRepository = rosterRepository;
    }

    public Role createNewRole (RoleNameEnum name) throws CreateRoleException{

            Role newRole = new Role(name);
            Role r = roleRepository.save(newRole);
            return r;


    }

    public Department createNewDepartment (String name) throws CreateDepartmentException {

            Department newDepartment = new Department(name);
            Department d = departmentRepository.save(newDepartment);
            return d;

    }

    //staff entity: first categoryName, last categoryName, nric, username&password(to be configured by admin),leave remaining
    //for HR to create staff. HR supplies, first categoryName, last categoryName, nric, address, bank details,
    //role, department.
    public Staff createNewStaff (Staff staff,Address staffAddress, Long roleId,Long departmentId)throws InputDataValidationException, CreateNewStaffException {
      validationService.throwExceptionIfInvalidBean(staff);
       validationService.throwExceptionIfInvalidBean(staffAddress);

        try{
           Staff existingStaff = null;

            try {
                existingStaff = retrieveStaffByNRIC(staff.getNric());
            } catch (StaffNotFoundException ex) {
            }

            if (existingStaff != null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("nric", ErrorMessages.NRIC_TAKEN);
                throw new InputDataValidationException(errorMap, ErrorMessages.NRIC_TAKEN);
            }

            //If staff does not exist
            //Set address, role and department before saving because of sql constraint
            //Address ID, role ID and department ID column cannot be empty
            addressRepository.save(staffAddress);
            Role r = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException("Role does not exist"));
            Department d = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department does not exist"));

            staff.setAddress(staffAddress);
            staff.setRole(r);
            staff.setDepartment(d);
            Staff savedStaff = staffRepository.save(staff);

            return lazyLoadStaffFields(savedStaff);


        } catch (PersistenceException | RoleNotFoundException | DepartmentNotFoundException ex) {
            throw new CreateNewStaffException(ex.getMessage());
        }
    }

    //For admin to configure staff account
    //staff username will be unique ID
    //After admin creates staff account, system sends staff via staff email the account username and password
    //No verification done here
    public Staff createNewStaffAccount(Long staffID) throws CreateNewStaffAccountException {

        try {
            Staff staff = retrieveStaffByStaffId(staffID);
            if(staff.getUsername()!=null){
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("staffId", ErrorMessages.STAFF_ACCOUNT_ALREADY_CONFIGURED);
                throw new CreateNewStaffAccountException(errorMap, ErrorMessages.STAFF_ACCOUNT_ALREADY_CONFIGURED);
            }
            staff.setUsername(staff.getFirstName() + staff.getLastName() + staffID.toString());

            //generate random password
            //password is encoded and stored in db
            //send staff the password(not the encoded one)
            String password = "password";
            staff.setPassword(encoder.encode(password));
            //dont need to save in repository because staff already saved when HR created.
//            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
//                //send an email to staff informing staff of username and password
//                sendEmail(staffID.toString(),password, "shawnroshan@gmail.com"); //TODO: to change to actual email
//            }

            return staff;
        }catch (StaffNotFoundException ex){
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("staffId", ErrorMessages.STAFF_DOES_NOT_EXIST);
            throw new CreateNewStaffAccountException(errorMap, ErrorMessages.STAFF_DOES_NOT_EXIST);
        }

    }

    //For IT department to reset for staff
    public Staff resetPassword(Long staffId) throws StaffNotFoundException{
        try {
            Staff staff = retrieveStaffByStaffId(staffId);

            String password = "password";
            staff.setPassword(encoder.encode(password));

//            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
//                //send an email to staff informing staff new password
//                sendEmail(staffId.toString(), password, "shawnroshan@gmail.com"); //TODO: to change to actual email
//            }

            return staff;
        }catch (StaffNotFoundException ex){
            throw new StaffNotFoundException("Staff does not exist!");
        }
    }

    //for HR to retrieve all staff
    public List<Staff> retrieveAllStaff() {
        List<Staff> allStaff= staffRepository.findAll();

        for (Staff staff : allStaff) {
            staff.getLeaves().size();
            staff.getRepliedReviews().size();
            staff.getRoster();
            staff.getRole();
            staff.getDepartment();
            staff.getPayrolls().size();
            staff.getDeliveries().size();
            staff.getBankDetails();
            staff.getAddress();
            staff.getAdvertisements().size();
        }
        return allStaff;
    }

    public List<Role>retrieveAllRoles(){
        List<Role>allRoles = roleRepository.findAll();
return allRoles;
}
    public List<Department> retrieveAllDepartments(){
        List<Department> allDepartments = departmentRepository.findAll();
        return allDepartments;

    }

    public Staff retrieveStaffByStaffId(Long staffId) throws StaffNotFoundException {
//        if (staffId == null) {
//            throw new StaffNotFoundException("Staff ID not provided");
//        }
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new StaffNotFoundException("Staff with id: " + staffId + " does not exist"));
        return lazyLoadStaffFields(staff);
    }

    private Staff retrieveStaffByNRIC(String nric) throws StaffNotFoundException {
        Staff staff = staffRepository.findByNric(nric)
                .orElseThrow(() -> new StaffNotFoundException("Staff NRIC: " + nric + "does not exist!"));

        return lazyLoadStaffFields(staff);
    }

    //For HR to update first categoryName, last categoryName, NRIC, username, bank details, department , role, address
    public Staff updateStaffDetails(Staff staff, Role role, Department department, Address address)throws UpdateStaffDetailsException, InputDataValidationException {
        validationService.throwExceptionIfInvalidBean(staff);
        validationService.throwExceptionIfInvalidBean(address);
        try {
            Staff staffToUpdate = retrieveStaffByStaffId(staff.getStaffId());
            addressRepository.save(address);

            staffToUpdate.setFirstName(staff.getFirstName());
            staffToUpdate.setLastName(staff.getLastName());
            staffToUpdate.setNric(staff.getNric());
            staffToUpdate.setEmail(staff.getEmail());
            staffToUpdate.setBankDetails(staff.getBankDetails());
            staffToUpdate.setDepartment(department);
            staffToUpdate.setRole(role);
            staffToUpdate.setAddress(address);

            return lazyLoadStaffFields(staffToUpdate);
        }catch (StaffNotFoundException ex) {
            throw new UpdateStaffDetailsException("Staff does not exist");
        }

    }

    public Staff retrieveStaffByUsername(String username) throws StaffNotFoundException {

        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new StaffNotFoundException("Staff username: " + username + "does not exist!"));

        return lazyLoadStaffFields(staff);
    }

    //staff logins with username
    public Staff staffLogin(String username, String password) throws InvalidStaffCredentialsException{
        try {
            Staff staff = retrieveStaffByUsername(username);
            //First statement for testing purposes because unable to retrieve unhashed password from staff object. 2nd statement for staff
            if (password.equals(staff.getPassword()) || encoder.matches(password, staff.getPassword())) {
                return lazyLoadStaffFields(staff);
            } else {
                System.out.println(password);
                System.out.println(staff.getPassword());
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("password", ErrorMessages.INCORRECT_PASSWORD);
                throw new InvalidStaffCredentialsException(errorMap,ErrorMessages.STAFF_LOGIN_FAILED);
            }

        } catch (StaffNotFoundException | java.lang.NumberFormatException ex) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("username", ErrorMessages.INCORRECT_USERNAME);
            throw new InvalidStaffCredentialsException(errorMap,ErrorMessages.STAFF_LOGIN_FAILED);
        }
    }

    public Staff changeStaffPassword(Long staffId, String oldPassword, String newPassword) throws StaffNotFoundException, InvalidStaffCredentialsException {
        try {
            Staff staff = retrieveStaffByStaffId(staffId);

            if (encoder.matches(oldPassword,staff.getPassword()) || oldPassword.equals(staff.getPassword())) {

                staff.setPassword(encoder.encode(newPassword));
                return retrieveStaffByStaffId(staffId);
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("password", ErrorMessages.OLD_PASSWORD_INCORRECT);
                throw new InvalidStaffCredentialsException(errorMap,ErrorMessages.OLD_PASSWORD_INCORRECT);
            }
        }catch(StaffNotFoundException ex){
            throw new StaffNotFoundException("Staff does not exist!");
        }
    }

    public Staff removeStaff(Long staffId) throws StaffNotFoundException, StaffCannotDeleteException {
        try {
            Staff existingStaff = retrieveStaffByStaffId(staffId);
            Address a = existingStaff.getAddress();

            if ((existingStaff.getAdvertisements() != null && existingStaff.getAdvertisements().size() >0)
                    || (existingStaff.getDeliveries() != null && existingStaff.getDeliveries().size()>0)){
                throw new StaffCannotDeleteException("Staff cannot be deleted due to existing associations "
                        + "(advertisments/deliveries) with the store");
            }

            // Clear relationship with payrolls, and delete payrolls
            for (Payroll p : existingStaff.getPayrolls()) {
                p.getStaff().getPayrolls().remove(p);
                p.setStaff(null);
                payrollRepository.delete(p);
            }
            existingStaff.setPayrolls(null);
            // ----------------------------------------------------

            // Clear relationship with leaves, and delete leaves
            for (StaffLeave l : existingStaff.getLeaves()) {
                l.getApplicant().getLeaves().remove(l);
                l.setApplicant(null);
                l.setApprover(null);
                l.setEndorser(null);
                staffLeaveRepository.delete(l);
            }
            existingStaff.setLeaves(null);
            // ----------------------------------------------------

            // Clear relationship with role and department
            existingStaff.setRole(null);
            existingStaff.setDepartment(null);
            // ----------------------------------------------------


            // Clear relationship with replied reviews, and delete replied reviews
            for (Review r : existingStaff.getRepliedReviews()) {
                r.getProduct().getReviews().remove(r);
                r.setProduct(null);
                r.getStaff().getRepliedReviews().remove(r);
                r.setStaff(null);
                reviewRepository.delete(r);
            }
            existingStaff.setRepliedReviews(null);;
            // ----------------------------------------------------

            // Clear relationship with advertisements, and delete advertisements
            //for (Advertisement ad : existingStaff.getAdvertisements()) {
              //  ad.getCreator().getAdvertisements().remove(ad);
              //  ad.setCreator(null);
             //   advertisementRepository.delete(ad);
          //  }
         //   existingStaff.setAdvertisements(null);
            // ----------------------------------------------------

            staffRepository.delete(existingStaff);
            // Delete address

            addressRepository.delete(a);
            // ----------------------------------------------------
            return existingStaff;


        } catch (StaffNotFoundException ex){
            throw new StaffNotFoundException("Staff not found");
        }



    }



    private Staff lazyLoadStaffFields(Staff staff) {
        staff.getAdvertisements().size();
        staff.getDeliveries().size();
        staff.getLeaves().size();
        staff.getPayrolls().size();
        staff.getRepliedReviews().size();

        return staff;
    }




    private void sendEmail(String username, String password, String email) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Here are your account details");
        msg.setText("Your Username:"+ username + " Your Password:" + password);
        javaMailSender.send(msg);
    }

    private void sendResetPassword(String password, String email) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your Password Has Been Reset");
        msg.setText("Your New Password is:" + password);
        javaMailSender.send(msg);
    }


}
