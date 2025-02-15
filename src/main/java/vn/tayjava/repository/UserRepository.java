package vn.tayjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.tayjava.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Custom Query
    @Query(value = "select u from user u inner join u.addresses a where a.city=:city")
    List<User> getAllUsers();

    // Distinct
    // @Query(value = "select distinct from User u where u.firstName=:firstName and u.lastName=:lastName")
    List<User> findDistinctByFirstNameAndLastName(String firstName, String lastName);

    // Single field
    //  @Query(value = "select * from User u where u.email=:email")
    List<User> findByEmail(String email);

    // -- OR --
    // @Query(value = "select * from User u where u.firstName=:lastName or u.lastName=:lastName")
    List<User> findByFirstNameOrLastName(String firstName, String lastName);

    // Is, Equals
    // @Query(value = "select * from User u where u.firstName:firstName")
    List<User> findByFirstNameIs(String firstName);
    List<User> findByFirstNameEquals(String firstName);
    List<User> findByFirstName(String firstName);

    // Between
    // @Query(value = "select * from User u where u.createdAt between ?1 and ?2")
    List<User> findByCreatedAtBetween(Date startDate, Date endDate);

    // LessThan
    // @Query(value = "select * from User u where u.age<:age")
    List<User> findByAgeLessThan(int age);
    List<User> findByAgeLessThanEqual(int age);
    List<User> findByAgeGreaterThanEqual(int age);
    List<User> findByAgeGreaterThan(int age);

    // Before After
    // @Query(value = "select * from User u where u.createdAt<:date")
    List<User> findByCreatedAtBefore(Date date);

    // IsNull, notNull
    List<User> findByAgeIsNull();
    List<User> findByAgeNotNull();

    // Like (khong tu dong them %)
    // khong co % thi cung giong equals
    // @Query(value = "select * from User u where u.lastName like %:lastName%")
    List<User> findByLastNameLike(String lastName);

    // StartingWith
    // @Query(value = "select * from User u where u.lastName like :lastName%")
    List<User> findByLastNameStartingWith(String lastName);

    // EndingWith
    // @Query(value = "select * from User u where u.lastName like %:lastName")
    List<User> findByLastNameEndingWith(String lastName);

    // Containing
    List<User> findByLastNameContaining(String name);

    // In
    // @Query(value = "select * from User u where u.age in (18,20,25)")
    List<User> findByAgeIn(Collection<Integer> age);

    // True/False
    List<User> findByActivated(Boolean activated);

    // IgnoreCase
    List<User> findByFirstNameIgnoreCase(String firstName);
}
