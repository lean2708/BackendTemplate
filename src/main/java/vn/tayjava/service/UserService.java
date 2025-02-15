package vn.tayjava.service;

import org.springframework.data.domain.Pageable;
import vn.tayjava.constants.UserStatus;
import vn.tayjava.dto.request.UserRequestDTO;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.model.User;

import java.util.List;

public interface UserService {
    long saveUser(UserRequestDTO request);
    void updateUser(long userId, UserRequestDTO request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    PageResponse<?> getAllUsers(int pageNo, int pageSize, String sortBy);
    PageResponse<?>  getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<?> getAllUsersWithSortByMultipleColumnsAndSearch(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    PageResponse<?> advanceSearchWithSpecification(Pageable pageable,  String[] user, String[] address);
}
