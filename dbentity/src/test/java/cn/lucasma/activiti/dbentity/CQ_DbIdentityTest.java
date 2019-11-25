package cn.lucasma.activiti.dbentity;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 身份信息服务
 */
public class CQ_DbIdentityTest {
    private static final Logger logger = LoggerFactory.getLogger(CQ_DbIdentityTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti-mysql.cfg.xml");

    @Test
    public void testIdentity(){
        IdentityService identityService = activitiRule.getIdentityService();
        // 创建用户1
        User user1 = identityService.newUser("user1");
        user1.setFirstName("firstName");
        user1.setLastName("lastName");
        user1.setEmail("user1@126.com");
        user1.setPassword("pwd");
        identityService.saveUser(user1);
        // 创建用户2
        User user2 = identityService.newUser("user2");
        identityService.saveUser(user2);
        Group group1 = identityService.newGroup("group1");
        group1.setName("for test");
        identityService.saveGroup(group1);
        // 创建用户群组关系
        identityService.createMembership(user1.getId(),group1.getId());
        identityService.createMembership(user2.getId(),group1.getId());
        // 拓展用户信息
        identityService.setUserInfo(user1.getId(),"age","18");
        identityService.setUserInfo(user1.getId(),"address","bei jing");


    }


}
