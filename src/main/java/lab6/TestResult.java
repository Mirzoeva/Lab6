package lab5;

import java.util.Optional;

public class TestResult {
    private final UrlTest Urltest;
    private final Long middleValue;

    public TestResult(UrlTest Urltest, Long middleValue){
        this.Urltest = Urltest;
        this.middleValue = middleValue;
    }

    public UrlTest getUrltest(){
        return Urltest;
    }

    public Long getMiddleValue(){
        return middleValue;
    }

    public Optional<TestResult> get(){
        return this.getMiddleValue() != null ? Optional.of(this) : Optional.empty();
    }
}
