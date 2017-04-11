package com.commissiongun.yore.commission;


import com.commissiongun.yore.util.DatabaseUtil;
import com.commissiongun.yore.util.PropertiesUtil;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Admin implements Serializable{

    // ERROR_MSG
    private static final String UNKNOWN_ERROR = "未知的错误";           // 这个不用解释了吧
    private static final String NO_SUCH_ACCOUNT = "用户名或密码错误";    // 登录错误

    // 商品价格
    private static int lockCost;
    private static int stocksCost;
    private static int barrelsCost;

    // 管理员身份信息
    private Long userId = null;
    private String userAccount = null;
    private String userPassword = null;
    private String userNickName = null;
    private Long userSignUpTime = null;     // 11位时间戳
    private int userType = 0;

    /**
     * 管理员类构造方法
     *
     * @param account
     * @param password
     */
    public Admin(String account, String password) {
        userAccount = account;
        userPassword = password;
        // 获取配置信息
        lockCost = Integer.parseInt(PropertiesUtil.getProperty("lock_cost"));
        stocksCost = Integer.parseInt(PropertiesUtil.getProperty("stocks_cost"));
        barrelsCost = Integer.parseInt(PropertiesUtil.getProperty("barrels_cost"));
    }

    /**
     * 管理员登录
     *
     * @return new String[] {"1", id, account, password, nickname, signUpTime} || {"0", errMsg}
     */
    public String[] login() {
        DatabaseUtil.getConnection();
        String[] info;
        String sql = "select * from cm_user where user_account=\'" + userAccount + "\'";
        ResultSet rs = DatabaseUtil.execSQL(sql);
        try {
            if (rs != null && rs.next()) {
                String password = rs.getString("user_password");
                if (!password.equals(userPassword)) return new String[]{"0", NO_SUCH_ACCOUNT};
                userId = rs.getLong("user_id");
                userAccount = rs.getString("user_account");
                userNickName = rs.getString("user_nickname");
                userSignUpTime = rs.getLong("user_sign_up_time");
                userType = rs.getInt("user_type");
            } else {
                return new String[]{"0", NO_SUCH_ACCOUNT};
            }
            info = new String[]{
                    "1",
                    userId.toString(),
                    userAccount,
                    userPassword,
                    userNickName,
                    userSignUpTime.toString(),
                    userType + "",
            };
        } catch (Exception e) {
            System.out.println("ERROR -> admin_login ");
            e.printStackTrace();
            return new String[]{"0", UNKNOWN_ERROR};
        }
        DatabaseUtil.closeConnection();
        return info;
    }

    /**
     * 获取当月的总销售额， 各商品的总销售额
     *
     * @return int[] {总销售额，lock销售额， stocks销售额， barrels销售额}
     */
    public int[] getSumOfAll() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String sql = "select sum(record_lock) as 'lock', sum(record_stocks) as 'stocks', sum(record_barrels) as 'barrels'" +
                " from cm_sale_record where year(record_time)='" + year + "' and month(record_time)='" + month + "'";

        try {
            ResultSet rs = DatabaseUtil.execSQL(sql);
            if (rs != null && rs.next()) {
                int lock = rs.getInt("lock");
                int stocks = rs.getInt("stocks");
                int barrels = rs.getInt("barrels");
                return new int[]{
                        lock * lockCost + stocks * stocksCost + barrels * barrelsCost,
                        lock * lockCost,
                        stocks * stocksCost,
                        barrels * barrelsCost
                };
            }
        } catch (Exception e) {
            System.out.println("ERROR -> get_sum_of_all ");
            e.printStackTrace();
        }
        return new int[]{0, 0, 0, 0};
    }

    /**
     * 获取当月的前5名销售精英
     *
     * @return ArrayList {String[] {id, 姓名, 销售总额},...}
     */
    public ArrayList<String[]> getTopFiveSalesManInfo() {
        ArrayList<String[]> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        String sql = "select sum(r.record_lock)*" + lockCost + "+sum(r.record_stocks)*" + stocksCost +
                "+sum(r.record_barrels)*" + barrelsCost + " as 'sum', u.user_id as 'id', u.user_nickname as nick" +
                " from cm_sale_record r right join cm_user u on r.record_user_id=u.user_id" +
                " where year(r.record_time)='" + year + "'" +
                " and month(r.record_time)='" + month + "'" +
                " and u.user_type=1 " +
                " group by id order by sum desc " +
                " limit 5";

        try {
            ResultSet rs = DatabaseUtil.execSQL(sql);
            while (rs != null && rs.next()) {
                int sum = rs.getInt("sum");
                int id = rs.getInt("id");
                String nickName = rs.getString("nick");
                result.add(new String[]{id + "", nickName, sum + ""});
            }
            return result;
        } catch (Exception e) {
            System.out.println("ERROR -> get_top_five_salesman ");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取所有销售人员的的筛选信息
     *
     * @param year     年
     * @param month    月
     * @param townName 城市名
     * @return ArrayList {String[] {id, 姓名, 销售额}}
     */
    public ArrayList<String[]> getAllSalesManInfo(int year, int month, String townName) {
        ArrayList<String[]> list = new ArrayList<>();
        String addition = "";
        if (year != 0) {
            addition += " year(r.record_time)=" + year +
                    " and month(r.record_time)=" + month;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH) + 1;
            addition += "year(r.record_time)=" + y +
                    " and month(r.record_time)=" + m;
        }
        if (townName != null && !townName.equals("")) {
            addition += " and r.record_town_name='" + townName + "'";
        }

        String sql = "select sum(r.record_lock)*" + lockCost + "+sum(r.record_stocks)*" + stocksCost +
                "+sum(r.record_barrels)*" + barrelsCost + " as 'sum', u.user_id as 'id', u.user_nickname as nick" +
                " from cm_sale_record r right join cm_user u on r.record_user_id=u.user_id" +
                " where " + addition +
                " and u.user_type=1 " +
                " group by id order by sum desc";

        try {
            ResultSet rs = DatabaseUtil.execSQL(sql);
            while (rs != null && rs.next()) {
                int sum = rs.getInt("sum");
                int id = rs.getInt("id");
                String nickName = rs.getString("nick");
                list.add(new String[]{id + "", nickName, sum + ""});
            }
        } catch (Exception e) {
            System.out.println("ERROR -> getAllSalesManInfo ");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取某销售人员的详细筛选信息
     *
     * @param year     年
     * @param month    月
     * @param townName 城市名
     * @param uid      销售人员id
     * @return ArrayList {String[] {时间, lock数, stocks数, barrels数, 城市名}}
     */
    public ArrayList<String[]> getSaleInfoByUser(int year, int month, String townName, int uid) {
        ArrayList<String[]> list = new ArrayList<>();
        int y = year;
        int m = month;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (y == 0) {
            y = cal.get(Calendar.YEAR);
            m = cal.get(Calendar.MONTH) + 1;
        }
        String where = "where record_user_id=" + uid + " and year(record_time)=" + y +
                " and month(record_time)=" + m + " ";
        if (townName != null && !townName.equals(""))
            where += " and record_town_name='" + townName + "'";
        String sql = "select * from cm_sale_record " + where;

        try {
            ResultSet rs = DatabaseUtil.execSQL(sql);
            while (rs != null && rs.next()) {
                String time = rs.getString("record_time");
                int lock = rs.getInt("record_lock");
                int stocks = rs.getInt("record_stocks");
                int barrels = rs.getInt("record_barrels");
                String town = rs.getString("record_town_name");
                list.add(new String[]{time, lock + "", stocks + "", barrels + "", town});
            }
        } catch (Exception e) {
            System.out.println("ERROR -> getSaleInfoByUser ");
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 获取待确认的销售人员-1信息
     *
     * @return ArrayList {String[] {end_id, 姓名, 日期, lock销售额, stocks销售额, barrels销售额},...}
     */
    public ArrayList<String[]> getUnCheckedData() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "select year(end_time) as 'y', month(end_time) as 'm',end_lock, end_stocks, end_barrels, end_id," +
                " user_nickname, user_id" +
                " from cm_end, cm_user where end_checked=0 " +
                " and cm_user.user_id=cm_end.end_user_id";

        try {
            ResultSet rs = DatabaseUtil.execSQL(sql);
            while (rs != null && rs.next()) {
                int y = rs.getInt("y");
                int m = rs.getInt("m");
                int lock = rs.getInt("end_lock");
                int stocks = rs.getInt("end_stocks");
                int barrels = rs.getInt("end_barrels");
                int end_id = rs.getInt("end_id");
                String nickname = rs.getString("user_nickname");
                list.add(new String[]{
                        end_id + "",
                        nickname,
                        y + "-" + m,
                        lock * lockCost + "",
                        stocks * stocksCost + "",
                        barrels * barrelsCost + ""
                });
            }
        } catch (Exception e) {
            System.out.println("ERROR -> get_unchecked_date ");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 对销售人员的-1进行确认，确认之后销售人员可以看到月报的统计信息
     *
     * @param end_id 数据库主键
     * @return success : true || false
     */
    public boolean doCheck(int end_id) {
        boolean success = false;

        String sql = "update cm_end set end_checked=1 where end_id=" + end_id;
        try {
            DatabaseUtil.execSQL(sql);
            success = true;
        } catch (Exception e) {
            System.out.println("ERROR -> do_check ");
            e.printStackTrace();
        }

        return success;
    }

    // 测试
    public static void test() {
        Admin admin = new Admin("admin", "12345");

        // login
        String[] loginInfo = admin.login();
        System.out.println("INFO -> test : login");
        for (String s : loginInfo) {
            System.out.println(s);
        }

        // 获取当月销售额统计
        int[] saleSum = admin.getSumOfAll();
        System.out.println("INFO -> test : sale_sum ");
        for (int i : saleSum) {
            System.out.print(i + "\t");
        }
        System.out.println();

        // 获取前五的销售精英信息
        ArrayList<String[]> top5 = admin.getTopFiveSalesManInfo();
        System.out.println("INFO -> get_top_5 ");
        for (String[] sl : top5) {
            System.out.println(sl[0] + " " + sl[1] + " " + sl[2]);
        }

        // 获取所有用户的销售信息
        ArrayList<String[]> list = admin.getAllSalesManInfo(0, 0, "");
        System.out.println("INFO -> get_all_salesman ");
        for (String[] sl : list) {
            System.out.println(sl[0] + " " + sl[1] + " " + sl[2]);
        }

        // 按时间筛选
        list = admin.getAllSalesManInfo(2017, 2, "");
        System.out.println("INFO -> get_all_salesman by time");
        for (String[] sl : list) {
            System.out.println(sl[0] + " " + sl[1] + " " + sl[2]);
        }

        // 获取某用户的详细信息
        list = admin.getSaleInfoByUser(0, 0, "", 8);
        System.out.println("INFO -> get_sale_info_by_user ");
        for (String[] sl : list) {
            System.out.println(sl[0] + " " + sl[1] + " " + sl[2] + " " + sl[3] + " " + sl[4]);
        }

        // 获取未确认
        list = admin.getUnCheckedData();
        System.out.println("INFO -> get_unchecked_data ");
        for (String[] sl : list) {
            System.out.println(sl[0] + " " + sl[1] + " " + sl[2] + " " + sl[3] + " " + sl[4] + " " + sl[5]);
        }

    }


    public static void main(String[] args) {
        test();
    }
}
