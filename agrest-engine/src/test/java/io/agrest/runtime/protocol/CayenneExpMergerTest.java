package io.agrest.runtime.protocol;

import io.agrest.AgException;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.base.protocol.CayenneExp;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExpressionParser;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.path.PathDescriptorManager;
import org.apache.cayenne.exp.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.jupiter.api.Assertions.*;

public class CayenneExpMergerTest {

    private static MetadataService metadataService;
    private static CayenneExpMerger merger;
    private ResourceEntity<Tr> entity;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        metadataService = new MetadataService(Collections.singletonList(compiler));

        PathDescriptorManager pathDescriptorManager = new PathDescriptorManager();
        merger = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathDescriptorManager));
    }

    @BeforeEach
    public void beforeEach() {
        entity = new RootResourceEntity<>(metadataService.getAgEntity(Tr.class), null);
    }

    @Test
    public void testMerge_Bare() {

        merger.merge(entity, new CayenneExp("a = 12345 and b = 'John Smith' and c = true"));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("a = 12345 and b = 'John Smith' and c = true"), e);
    }

    @Test
    public void testMerge_Functions() {

        merger.merge(entity, new CayenneExp("length(b) > 5"));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("length(b) > 5"), e);
    }

    @Test
    public void testMerge_List_Params_String() {

        merger.merge(entity, new CayenneExp("b=$s", "x"));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("b='x'"), e);
    }

    @Test
    public void testMerge_List_Params_Multiple() {

        merger.merge(entity, new CayenneExp("b=$s or b =$x or b =$s", "x", "y"));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("b='x' or b='y' or b='x'"), e);
    }

    @Test
    public void testMerge_Map_Params_String() {

        merger.merge(entity, new CayenneExp("b=$s", Collections.singletonMap("s", "x")));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("b='x'"), e);
    }

    @Test
    public void testMerge_Map_Params_Int() {

        merger.merge(entity, new CayenneExp("a=$n", Collections.singletonMap("n", 453)));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("a=453"), e);
    }

    @Test
    public void testMerge_Map_Params_Double() {

        merger.merge(entity, new CayenneExp("d=$n", Collections.singletonMap("n", 4.4009)));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("d=4.4009"), e);
    }

    @Test
    public void testMerge_Map_Params_Doublet_Negative() {
        merger.merge(entity, new CayenneExp("d=$n", Collections.singletonMap("n", -4.4009)));
        Expression e = entity.getQualifier();

        assertNotNull(e);

        // Cayenne parses 'fromString' as ASTNegate(ASTScalar), so to compare
        // apples to apples, let's convert it back to String.. not an ideal
        // comparison, but a good approximation
        assertEquals("d = -4.4009", e.toString());
    }

    @Test
    public void testMerge_Map_Params_Boolean_True() {

        merger.merge(entity, new CayenneExp("c=$b", Collections.singletonMap("b", true)));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("c=true"), e);
    }

    @Test
    public void testMerge_Map_Params_Boolean_False() {
        merger.merge(entity, new CayenneExp("c=$b", Collections.singletonMap("b", false)));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("c=false"), e);
    }

    @Test
    public void testMerge_Params_InvalidPath() {
        CayenneExp exp = new CayenneExp("invalid/path=$b", Collections.singletonMap("b", false));
        assertThrows(AgException.class, () -> merger.merge(entity, exp));
    }

    @Test
    public void testMerge_Map_Params_Null() {
        merger.merge(entity, new CayenneExp("c=$b", Collections.singletonMap("b", null)));
        Expression e = entity.getQualifier();

        assertNotNull(e);
        assertEquals(exp("c=null"), e);
    }

    @Test
    public void testMerge_Map_Params_Date_NonISO() {
        CayenneExp exp = new CayenneExp("e=$d", Collections.singletonMap("d", "2014:02:03"));
        assertThrows(AgException.class, () -> merger.merge(entity, exp));
    }

    @Test
    public void testMerge_Map_Params_Date_Local_TZ() {
        merger.merge(entity, new CayenneExp("e=$d", Collections.singletonMap("d", "2014-02-03T14:06:35")));
        Expression e = entity.getQualifier();

        assertNotNull(e);

        GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
        cal.setTimeZone(TimeZone.getDefault());
        Date date = cal.getTime();

        Expression expected = exp("e=$d", date);
        assertEquals(expected, e);
    }

    @Test
    public void testMerge_Map_Params_Date_TZ_Zulu() {

        merger.merge(entity, new CayenneExp("e=$d", Collections.singletonMap("d", "2014-02-03T22:06:35Z")));

        Expression e = entity.getQualifier();

        assertNotNull(e);

        GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
        cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date date = cal.getTime();

        Expression expected = exp("e=$d", date);
        assertEquals(expected, e);
    }

    @Test
    public void testMerge_Map_Params_Date_TZ_Zulu_DST() {

        merger.merge(entity, new CayenneExp("e=$d", Collections.singletonMap("d", "2013-06-03")));
        Expression e = entity.getQualifier();

        assertNotNull(e);

        GregorianCalendar cal = new GregorianCalendar(2013, 5, 3);
        cal.setTimeZone(TimeZone.getDefault());
        Date date = cal.getTime();

        Expression expected = exp("e=$d", date);
        assertEquals(expected, e);
    }

    @Test
    public void testMerge_Map_Params_Date_NoTime() {
        merger.merge(entity, new CayenneExp("e=$d", Collections.singletonMap("d", "2013-06-03T22:06:35Z")));
        Expression e = entity.getQualifier();

        assertNotNull(e);

        GregorianCalendar cal = new GregorianCalendar(2013, 5, 3, 15, 6, 35);
        cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date date = cal.getTime();

        Expression expected = exp("e=$d", date);
        assertEquals(expected, e);
    }

    @Test
    public void testMerge_DisallowDBPath() {
        CayenneExp exp = new CayenneExp("db:id=$i", Collections.singletonMap("i", 5));
        assertThrows(AgException.class, () -> merger.merge(entity, exp));
    }

    @Test
    public void testMerge_MatchByRootId() {
        merger.merge(entity, new CayenneExp("id=$i", Collections.singletonMap("i", 5)));
        Expression e = entity.getQualifier();
        assertEquals(exp("id=$i", 5), e);
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public boolean getC() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public double getD() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public Date getE() {
            throw new UnsupportedOperationException();
        }
    }
}
