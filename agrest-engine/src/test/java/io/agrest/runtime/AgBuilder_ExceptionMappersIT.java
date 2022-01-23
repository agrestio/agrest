package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.SelectStage;
import io.agrest.junit.AgPojoTester;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P2;
import io.agrest.spi.AgExceptionMapper;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.di.Module;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgBuilder_ExceptionMappersIT {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester
            .builder()
            .agCustomizer(b -> b.module(exceptionsModule()))
            .build();

    private static Module exceptionsModule() {
        return cb -> cb
                .bindMap(AgExceptionMapper.class)
                .put(AgException.class.getName(), TestAgExceptionMapper.class)
                .put(TestException.class.getName(), TestExceptionMapper.class);
    }

    @Test
    public void testExceptionMapper_OverrideStandardMapper() {

        try {
            tester.ag().select(P1.class)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        throw AgException.forbidden("_ag_exception_");
                    })
                    .get();
        } catch (AgException e) {
            assertEquals("", e.getMessage());
        }
    }

    @Test
    public void testExceptionMapper_CustomExceptionMapper() {

        try {
            tester.ag().select(P2.class)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        throw new TestException("_test_exception_");
                    })
                    .get();
        } catch (AgException e) {
            assertEquals("", e.getMessage());
        }
    }

    public static class TestAgExceptionMapper implements AgExceptionMapper<AgException> {

        @Override
        public AgException toAgException(AgException e) {
            return AgException.internalServerError(e, "_ag_%s", e.getMessage());
        }
    }

    public static class TestExceptionMapper implements AgExceptionMapper<TestException> {

        @Override
        public AgException toAgException(TestException e) {
            return AgException.internalServerError(e, "_test_%s", e.getMessage());
        }
    }

    public static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }
}
