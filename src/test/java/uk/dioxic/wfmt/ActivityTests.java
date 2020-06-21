package uk.dioxic.wfmt;

public class ActivityTests {

//    private static MongoClient client;
//    private static ActivityService activityService;
//    private static UserService userService;
//    private static RegionService regionService;
//    private static OrderService orderService;
//
//    private User user;
//    private Region region1;
//    private Region region2;
//
//    @BeforeAll
//    static void setup() {
//        client = MongoUtil.createPojoClient();
//        activityService = new ActivityService("test", client);
//        userService = new UserService("test", client);
//        regionService = new RegionService("test", client);
//        orderService = new OrderService("test", client);
//    }
//
//    @BeforeEach
//    void createData() {
//        userService.drop();
//        regionService.drop();
//        activityService.drop();
//        orderService.drop();
//
//        user = new User();
//        user.setFirstName("Bob");
//        user.setLastName("Duck");
//        user.setUserId(ObjectId.get());
//
//        userService.insert(user);
//
//        region1 = new Region();
//        region1.setName("London");
//        region1.setRegionId(ObjectId.get());
//
//        region2 = new Region();
//        region2.setName("Mumbai");
//        region2.setRegionId(ObjectId.get());
//
//        regionService.insert(region1);
//        regionService.insert(region2);
//    }
//
//    @Test
//    void insert() {
//        Activity activity1 = createActivity("A1");
//        activityService.insert(activity1);
//
//        Activity activity2 = createActivity("A2");
//        activityService.insert(activity2);
//    }
//
//    @Test
//    void update_chargeableEcc() {
//        Activity activity1 = createActivity("A1");
//        activityService.insert(activity1);
//
//        Activity activity2 = createActivity("A2");
//        activityService.insert(activity2);
//
//        activity2.setChargeableEcc("UPD");
//        activityService.update(activity2);
//    }
//
//    @Test
//    void update_region() {
//        Activity activity1 = createActivity("A1");
//        activityService.insert(activity1);
//
//        Activity activity2 = createActivity("A2");
//        activityService.insert(activity2);
//
//        activity2.setRegionId(region2.getRegionId());
//        activityService.update(activity2);
//    }
//
//    Activity createActivity(String id) {
//        Activity activity = new Activity();
//        activity.setActivityId(id);
//        activity.setActualEcc("ECC");
//        activity.setChargeableEcc("C");
//        activity.setOrderId("ORDER1");
//        activity.setRegionId(region1.getRegionId());
//        return activity;
//    }

}
