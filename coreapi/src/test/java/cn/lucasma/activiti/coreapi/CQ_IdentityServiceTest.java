package cn.lucasma.activiti.coreapi;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.util.List;

/**
 * 身份服务 identityService
 */
public class CQ_IdentityServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CQ_IdentityServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    /**
     * identityService 并不依赖流程文件
     * 用户和用户组是多对多关系
     */
    @Test
    public void testIdentity() {
        IdentityService identityService = activitiRule.getIdentityService();

        User user1 = identityService.newUser("user1");
        user1.setEmail("user1@qq.com");
        user1.setLastName("name1");

        User user2 = identityService.newUser("user2");
        user2.setEmail("user2@qq.com");
        user2.setLastName("name2");

        identityService.saveUser(user1);
        identityService.saveUser(user2);

        Group group1 = identityService.newGroup("group1");
        group1.setName("一组");
        Group group2 = identityService.newGroup("group2");
        group2.setName("二组");

        identityService.saveGroup(group1);
        identityService.saveGroup(group2);

        // 设置用户和用户组关系
        identityService.createMembership("user1", "group1");
        identityService.createMembership("user2", "group1");
        identityService.createMembership("user1", "group2");
        // 修改用户LastName
        User user11 = identityService.createUserQuery().userId("user1").singleResult();
        user11.setLastName("chenqiang");
        identityService.saveUser(user11);

        List<User> userList = identityService.createUserQuery().memberOfGroup("group1").listPage(0, 100);
        for (User user : userList) {
            logger.info("user = {}", ToStringBuilder.reflectionToString(user, ToStringStyle.JSON_STYLE));
        }

        List<Group> groupList = identityService.createGroupQuery().groupMember("user1").listPage(0, 100);
        for (Group group : groupList) {
            logger.info("group = {}", ToStringBuilder.reflectionToString(group, ToStringStyle.JSON_STYLE));
        }
    }

}
