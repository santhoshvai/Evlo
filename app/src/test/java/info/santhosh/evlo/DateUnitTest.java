package info.santhosh.evlo;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class DateUnitTest {

    @Test
    public void equals_isCorrect() throws Exception {
        String startDateString = "28/05/2017";
        String endDateString = "28/05/2017";

        // This object can interpret strings representing dates in the format MM/dd/yyyy
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        // Convert from String to Date
        Date startDate = df.parse(startDateString);
        Date endDate = df.parse(endDateString);
        assertEquals(startDate, endDate);
    }

    @Test
    public void greater_isCorrect() throws Exception {
        String startDateString = "28/05/2017";
        String endDateString = "29/06/2017";

        // This object can interpret strings representing dates in the format MM/dd/yyyy
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        // Convert from String to Date
        Date startDate = df.parse(startDateString);
        Date endDate = df.parse(endDateString);
        assertTrue(endDate.after(startDate));
    }
}
