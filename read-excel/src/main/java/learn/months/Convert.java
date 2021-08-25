package learn.months;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Convert {
    public static void main(String[] args) {
        DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
        System.out.println("Rp " + DECIMAL_FORMAT.format(10000000).replace(',', '.'));
        System.out.println("Rp " + DECIMAL_FORMAT.format(new BigDecimal("10000000")).replace(',', '.'));
        System.out.println("Rp " + DECIMAL_FORMAT.format(new BigDecimal(1000000L)).replace(',', '.'));

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
        System.out.println(dateFormat1.format(System.currentTimeMillis()));

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("in"));
        System.out.println(dateFormat2.format(System.currentTimeMillis()));

        System.out.println("******");
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            calendar.set(2021, i, 10);
            System.out.println(dateFormat1.format(calendar.getTime()));
            System.out.println(dateFormat2.format(calendar.getTime()));
        }

        System.out.println("******");
        Map<String, String> map = new HashMap();
        map.put("DD Mmm YYYY HH:MM (WIB)", new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH).format(System.currentTimeMillis()));
        map.put("DD Mmm YYYY", new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(System.currentTimeMillis()));
        System.out.println(map.get("DD Mmm YYYY HH:MM (WIB)"));
        System.out.println(map.get("DD Mmm YYYY"));

        Map<String, String> monthMap = new HashMap();
        monthMap.put("May", "Mei");
        monthMap.put("Aug", "Agu");
        monthMap.put("Oct", "Okt");
        monthMap.put("Dec", "Des");

        if (map.containsKey("DD Mmm YYYY HH:MM (WIB)") || map.containsKey("DD Mmm YYYY")) {
            for (Map.Entry<String, String> entry : monthMap.entrySet()) {
                map.put("DD Mmm YYYY HH:MM (WIB)", map.get("DD Mmm YYYY HH:MM (WIB)").replace(entry.getKey(), entry.getValue()));
                map.put("DD Mmm YYYY", map.get("DD Mmm YYYY").replace(entry.getKey(), entry.getValue()));
            }
        }
        System.out.println(map.get("DD Mmm YYYY HH:MM (WIB)"));
        System.out.println(map.get("DD Mmm YYYY"));
    }
}
