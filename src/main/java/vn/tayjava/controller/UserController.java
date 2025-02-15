package vn.tayjava.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tayjava.configuration.Translator;
import vn.tayjava.constants.UserStatus;
import vn.tayjava.dto.request.UserRequestDTO;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.ResponseData;
import vn.tayjava.dto.response.ResponseError;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.exception.ErrorResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User Controller")
public class UserController {

    private final UserService userService;

    private static final String ERROR_MESSAGE = "errorMessage={}";

    @Operation(method = "POST", summary = "Add new user", description = "Send a request via this API to create new user")
    @PostMapping(value = "/")
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO request) {
        log.info("Request add user, {} {}", request.getFirstName(), request.getLastName());

        try {
            long userId = userService.saveUser(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Add user fail");
        }
    }

    @Operation(summary = "Update User", description = "Api update user")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@PathVariable @Min(1) int userId, @Valid @RequestBody UserRequestDTO user) {
        log.info("Request update userId={}", userId);
        try{
            userService.updateUser(userId, user);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.upd.success"));
        }
        catch (ResourceNotFoundException e) {
            log.error("errorMessage:{}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/{userId}")
    public ResponseData<?> updateStatus(@Min(1) @PathVariable long userId, @RequestParam UserStatus status) {
        log.info("Request change status, userId={}", userId);
        try{
            userService.changeStatus(userId, status);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.status.success"));
        }
        catch (ResourceNotFoundException e) {
            log.error("errorMessage:{}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @Operation(summary = "Delete User", description = "Api Delete user")
    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") long userId) {
        log.info("Request delete userId={}", userId);
        try{
            userService.deleteUser(userId);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), Translator.toLocale("user.del.success"));
        }
        catch (ResourceNotFoundException e) {
            log.error("errorMessage:{}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(1) long userId) {
        log.info("Request get user detail, userId={}", userId);
        try{
            return new ResponseData<>(HttpStatus.OK.value(), "user", userService.getUser(userId));
        } catch (ResourceNotFoundException e) {
            log.error("errorMessage:{}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Fetch All User")
    @GetMapping("/list")
    public ResponseData<PageResponse<?>> getAllUser(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                    @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                    @RequestParam(required = false) String sortBy) {
        log.info("Request get all of users");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsers(pageNo, pageSize, sortBy));
    }

    @Operation(summary = "Get list of users with sort by Multiple Columns")
    @GetMapping("/list-with-sort-by-multiple-columns")
    public ResponseData<?> getAllUsersWithSortByMultipleColumns(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                @RequestParam(required = false) String... sorts) {
        log.info("Request get all of users by Multiple Columns");
        return new ResponseData<>(HttpStatus.OK.value(),"users", userService.getAllUsersWithSortByMultipleColumns(pageNo, pageSize, sorts));
    }
    @Operation(summary = "Get list of users and search with paging and sorting by Customize Query")
    @GetMapping("/list-with-sort-by-multiple-columns-search")
    public ResponseData<?> getAllUsersWithSortByMultipleColumnsAndSearch(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                         @RequestParam(required = false) String search,
                                                                @RequestParam(required = false) String sortBy) {
        log.info("Request get all of users by Customize Query");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsersWithSortByMultipleColumnsAndSearch(pageNo, pageSize, search, sortBy));
    }

    @Operation(summary = "Get list of users with paging and sort by columns CRITERIA")
    @GetMapping("/advance-search-with-criteria")
    public ResponseData<?> advanceSearchByCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                         @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                         @RequestParam(required = false) String sortBy,
                                                                         @RequestParam(required = false) String address,
                                                                         @RequestParam(required = false) String... search) {
        log.info("Request get all of users by multiple CRITERIA");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchByCriteria(pageNo, pageSize, sortBy, address, search));
    }

    @Operation(summary = "Advance search query by Specification")
    @GetMapping("/advance-search-with-specification")
    public ResponseData<?> advanceSearchWithSpecification(Pageable pageable,
                                                   @RequestParam(required = false) List<String> user,
                                                   @RequestParam(required = false) List<String> address) {
        log.info("Advance search query by Specification");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchWithSpecification(pageable, user, address));
    }
}
