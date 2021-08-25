package learn.sql;

import java.text.DecimalFormat;

/**
 * @Author: wenhongliang
 */
public class GenSql {
    public static void main(String[] args) {
        //String[] strs = new String[]{"ar", "email", "pn", "sms"};
        //for (int type = 0; type < strs.length; type++) {
        //String STR_FORMAT = strs[type] + "_tab_00000000";
        String STR_FORMAT = "email_tab_00000000";
        //String STR_FORMAT = "email_tab_00000000";
        for (int i = 1; i < 100; i++) {
            DecimalFormat df = new DecimalFormat(STR_FORMAT);
            String tabName = df.format(i);
            //System.out.println("TRUNCATE TABLE " + tabName + ";");
            System.out.println("ALTER TABLE " + tabName + " ADD COLUMN `to_address` varchar(4000) NOT NULL DEFAULT '' COMMENT 'Receive address';");
            //System.out.println("DROP TABLE IF EXISTS `" + tabName + "`;");
            //System.out.println("CREATE TABLE `" + tabName + "` LIKE `" + "ar_tab_00000000`;");
            //System.out.println("ALTER TABLE " + tabName + " ADD COLUMN `ar_redirect_label` varchar(50) NOT NULL DEFAULT '' COMMENT 'ar redirect label';");
        }
        //}
    }
}
