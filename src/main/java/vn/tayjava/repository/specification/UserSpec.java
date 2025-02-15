package vn.tayjava.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.tayjava.constants.Gender;
import vn.tayjava.model.User;

// Tao doi tuong de tai su dung
public class UserSpec {

    // Dieu kien 1
    public static Specification<User> hashFirstName(String firstName){
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%");
    }
    // Dieu kien 2
    public static Specification<User> notEqualGender(Gender gender){
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("gender"), gender);
    }

    // cung giong
//    public static Specification<User> notEqualGender(Gender gender){
//        return new Specification<User>() {
//            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder){
//                return criteriaBuilder.notEqual(root.get("gender"), gender);
//            }
//        };
//    }
}
