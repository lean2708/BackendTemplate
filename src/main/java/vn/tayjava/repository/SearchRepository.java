package vn.tayjava.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.criteria.SearchCriteria;
import vn.tayjava.repository.criteria.UserSearchCriteriaQueryConsumer;
import vn.tayjava.repository.specification.SpecSearchCriteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vn.tayjava.repository.specification.SearchOperation.*;

@Slf4j
@Repository
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getAllUsersWithSortByMultipleColumnsAndSearch(int pageNo, int pageSize, String search, String sortBy){
        // query sang list user
        StringBuilder sqlQuery = new StringBuilder("Select new vn.tayjava.dto.response.UserDetailResponse(u.id, u.firstName, u.lastName, u.email, u.phone) " +
                "from user u where 1=1");

        if(StringUtils.hasLength(search)) {
            sqlQuery.append(" and lower(u.firstName) like lower(:firstName)");
            sqlQuery.append(" or lower(u.lastName) like lower(:lastName)");
            sqlQuery.append(" or lower(u.email) like lower(:email)");
        }

        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                sqlQuery.append(String.format("order by %s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);

        if(StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", "%" + search + "%");
            selectQuery.setParameter("lastName", "%" + search + "%");
            selectQuery.setParameter("email", "%" + search + "%");
        }

        List<User> users = selectQuery.getResultList();

        // Query paging
        StringBuilder sqlCountQuery = new StringBuilder("Select count(*) from user u where 1=1");

        if(StringUtils.hasLength(search)) {
            sqlCountQuery.append(" and lower(u.firstName) like lower(:firstName)");
            sqlCountQuery.append(" or lower(u.lastName) like lower(:lastName)");
            sqlCountQuery.append(" or lower(u.email) like lower(:email)");
        }

        // Count Users
        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        if(StringUtils.hasLength(search)) {
            selectCountQuery.setParameter("firstName", "%" + search + "%");
            selectCountQuery.setParameter("lastName", "%" + search + "%");
            selectCountQuery.setParameter("email", "%" + search + "%");
        }
        Long totalElements = (Long) selectCountQuery.getSingleResult();

        Page<?> page = new PageImpl<Object>(Collections.singletonList(users), PageRequest.of(pageNo,pageSize), totalElements);

        return PageResponse.builder()
                .page(pageNo)
                .size(pageSize)
                .total(page.getTotalPages())
                .items(users)
                .build();
    }

    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search){

        List<SearchCriteria> criteriaList = new ArrayList<>();

        // Lay dang sach user
        if (search != null) {
            for (String s : search) {
                log.info("sortBy: {}", sortBy);
                // firstName:asc|desc
                Pattern pattern = Pattern.compile("(\\w+?)(:|>|<)(.*)");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        // Lay so luong ban ghi (phan trang)
        List<User> users = getUsers(pageNo, pageSize, criteriaList, sortBy, address);

        Long totalElements = getTotalElements(criteriaList, address);

        return PageResponse.builder()
                .page(pageNo) // vi tri bang ghi trong danh sach khac JPA
                .size(pageSize)
                .total(totalElements.intValue()) // total elements
                .items(users)
                .build();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        // Xu ly cac dieu kien tim kiem
        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if(StringUtils.hasLength(address)){
            Join<Address, User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" + address + "%");
            // Tim kiem tren tat ca field cua address
            query.select(criteriaBuilder.count(root));
            query.where(predicate, addressPredicate);
        }
        else{
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
            query.select(criteriaBuilder.count(root));
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();
    }
    public PageResponse getUserJoinedAddress(Pageable pageable, String[] user, String[] address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Join<Address, User> addressUserJoin = userRoot.join("addresses");

        // build query
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addressPre = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+?)([:<>~!])(.*)(\\p{Punct}?)(.*)(\\p{Punct}?)");
        // user
        for(String u : user){
            Matcher matcher = pattern.matcher(u);
            if(matcher.find()){
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1),matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toUserPredicate(userRoot,criteriaBuilder, criteria);
                userPre.add(predicate);
            }
        }

        // address
        for(String a : address){
            Matcher matcher = pattern.matcher(a);
            if(matcher.find()){
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1),matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toAddressPredicate(addressUserJoin, criteriaBuilder, criteria);
                addressPre.add(predicate);
            }
        }

        // dieu kien (and/or)
        Predicate userPredicateArr = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
        Predicate addressPredicateArr = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
        Predicate finalPredicate = criteriaBuilder.and(userPredicateArr, addressPredicateArr);
        query.where(finalPredicate);

        List<User> users =  entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long count = count(user, address);

        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total((int) count)
                .items(users)
                .build();
        }

        private long count(String[] user, String[] address){
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class); // doi query thanh long
            Root<User> userRoot = query.from(User.class);

            Join<Address, User> addressUserJoin = userRoot.join("addresses");

            // build query
            List<Predicate> userPre = new ArrayList<>();
            List<Predicate> addressPre = new ArrayList<>();

            Pattern pattern = Pattern.compile("(\\w+?)([:<>~!])(.*)(\\p{Punct}?)(.*)(\\p{Punct}?)");
            // user
            for(String u : user){
                Matcher matcher = pattern.matcher(u);
                if(matcher.find()){
                    SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1),matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                    Predicate predicate = toUserPredicate(userRoot,criteriaBuilder, criteria);
                    userPre.add(predicate);
                }
            }

            // address
            for(String a : address){
                Matcher matcher = pattern.matcher(a);
                if(matcher.find()){
                    SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1),matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                    Predicate predicate = toAddressPredicate(addressUserJoin, criteriaBuilder, criteria);
                    addressPre.add(predicate);
                }
            }

            // dieu kien (and/or)
            Predicate userPredicateArr = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
            Predicate addressPredicateArr = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
            Predicate finalPredicate = criteriaBuilder.and(userPredicateArr, addressPredicateArr);

            query.select(criteriaBuilder.count(userRoot));
            query.where(finalPredicate);

            return  entityManager.createQuery(query).getSingleResult();
        }

    public Predicate toUserPredicate(@NotNull Root<User> root, @NotNull CriteriaBuilder criteriaBuilder,@NotNull SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()){
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        };
    }

    public Predicate toAddressPredicate(@NotNull Join<Address, User> root,@NotNull CriteriaBuilder criteriaBuilder,@NotNull SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()){
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        };
    }

    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sortBy, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        // Xu ly cac dieu kien tim kiem
        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if(StringUtils.hasLength(address)){
            Join<Address, User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" + address + "%");
            query.where(predicate, addressPredicate);
        }
        else{
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
            query.where(predicate);
        }

        // Sort
        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if(matcher.group(3).equalsIgnoreCase("desc")){
                    query.orderBy(criteriaBuilder.desc(root.get(columnName)));
                }else{
                    query.orderBy(criteriaBuilder.asc(root.get(columnName)));
                }
            }
        }

            return entityManager.createQuery(query)
                    .setFirstResult(pageNo)
                    .setMaxResults(pageSize)
                    .getResultList();
    }
}
