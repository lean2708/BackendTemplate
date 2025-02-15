package vn.tayjava.repository.specification;

import lombok.Getter;

import static vn.tayjava.repository.specification.SearchOperation.OR_PREDICATE_FLAG;
import static vn.tayjava.repository.specification.SearchOperation.ZERO_OR_MORE_REGEX;

@Getter
public class SpecSearchCriteria {
    private String key;  // firstName, lastName, ....
    private SearchOperation operation;  // toan tu
    private Object value;
    private Boolean orPredicate; // xd xem su dung or hay khong

    public SpecSearchCriteria(String key, SearchOperation operation, String value){
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
    public SpecSearchCriteria(String orPredicate, String key, SearchOperation operation, Object value){
        super();
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(String key, String operation, String value, String prefix, String suffix){
        SearchOperation  oper = SearchOperation.getSimpleOperation(operation.charAt(0));
        if(oper == SearchOperation.EQUALITY){
            boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
            boolean endWithAsterisk = prefix != null && suffix.contains(ZERO_OR_MORE_REGEX);
            if(startWithAsterisk && endWithAsterisk){ // *value*
                oper = SearchOperation.CONTAINS;
            }else if(startWithAsterisk){ // *value
                oper = SearchOperation.ENDS_WITH;
            }else if (endWithAsterisk){ // value*
                oper = SearchOperation.STARTS_WITH;
            }
        }
        this.key = key;
        this.operation = oper;
        this.value = value;
    }
}
