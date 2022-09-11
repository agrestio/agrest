package io.agrest.jaxrs3;

import io.agrest.DataResponse;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GET_Pojo_DataResponseIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void test() {
        tester.target("/data-response")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"a\":1,\"b\":\"two\",\"c\":{\"a\":5},\"d\":[{\"a\":6}]}",
                        "{\"a\":100,\"b\":\"two hundred\",\"c\":{\"a\":50},\"d\":[{\"a\":60}]}");
    }

    @Path("data-response")
    public static class Resource {

        @GET
        public DataResponse<X> xs() {
            // generate response bypassing Agrest stack
            return DataResponse.of(List.of(
                    new X(1, "two", new Y(5), List.of(new Y(6))),
                    new X(100, "two hundred", new Y(50), List.of(new Y(60))))).build();
        }
    }

    public static class X {
        private final int a;
        private final String b;
        private final Y c;
        private final List<Y> d;

        public X(int a, String b, Y c, List<Y> d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public Y getC() {
            return c;
        }

        public List<Y> getD() {
            return d;
        }
    }

    public static class Y {
        private final int a;

        public Y(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }
    }
}