package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 任务管理 taskService
 */
public class CQ_TaskServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CQ_TaskServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    /***
     * 测试taskService
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskService() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        // message 参数
        variables.put("message", "chenqiang message Test!!!");
        // 流程启动
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process",variables);

        TaskService taskService = activitiRule.getTaskService();
        // 获取任务
        Task task = taskService.createTaskQuery().singleResult();
        // 打印输出一下
        logger.info("task = {}", task);
        logger.info("task.documentation = {}", task.getDescription()); // 任务描述

        // 对比三种方式获取变量的区别

        taskService.setVariable(task.getId(), "key1", "value1");
        taskService.setVariableLocal(task.getId(), "localKey1", "localValue1");

        Map<String, Object> taskServiceVariables = taskService.getVariables(task.getId());
        Map<String, Object> taskServiceVariablesLocal = taskService.getVariablesLocal(task.getId());

        Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
        logger.info("taskServiceVariables = {}", taskServiceVariables);
        logger.info("taskServiceVariablesLocal = {}", taskServiceVariablesLocal);
        logger.info("processVariables = {}", processVariables); // 获取不到本地变量

        // 完成任务
        Map<String, Object> completeVariables = Maps.newHashMap();
        completeVariables.put("cKey1", "cValue1");
        taskService.complete(task.getId(), completeVariables);

        Task task1 = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        logger.info("task1 = {}", task1);
    }

    /**
     *  任务处理人测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskServiceUser() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        // message 参数
        variables.put("message", "chenqiang message Test!!!");
        // 流程启动
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process",variables);

        TaskService taskService = activitiRule.getTaskService();
        // 获取任务
        Task task = taskService.createTaskQuery().singleResult();
        // 打印输出一下
        logger.info("task = {}", task);
        logger.info("task.documentation = {}", task.getDescription()); // 任务描述

        taskService.setOwner(task.getId(), "user1");
        /*
            setAssignee 可以设置代理人，但是一般不用这个方法，如果任务已经指定了代理人，
            用这个方法设置则会覆盖掉之前的代理人，一般使用claim来设置
         */
//        taskService.setAssignee(task.getId(), "user1");
        // 查询指定了自己为候选人，并且还没有指定代理人的任务
        List<Task> taskList = taskService.createTaskQuery().taskCandidateUser("chenqiang").
                taskUnassigned().listPage(0, 100);
        // 指定代理人为自己
        for (Task task1 : taskList) {
            try {
                taskService.claim(task1.getId(), "chenqiang");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        // 查询流程相关身份
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink : identityLinksForTask) {
            logger.info("identityLink = {}", identityLink);
        }

        // 结束代理人为自己的流程
        List<Task> chenqiangTaskList = taskService.createTaskQuery()
                .taskAssignee("chenqiang").listPage(0, 100);
        for (Task task1 : chenqiangTaskList) {
            Map<String, Object> vars = Maps.newHashMap();
            vars.put("ckey1", "cvalue1");
            taskService.complete(task1.getId(), vars);
        }
        List<Task> chenqiang = taskService.createTaskQuery()
                .taskAssignee("chenqiang").listPage(0, 100);
        // 执行完成后打印任务是否存在
        logger.info("chenqiang task 是否存在 = {}", CollectionUtil.isNotEmpty(chenqiang));
    }


    /**
     * 附件测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskAttachment() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        // message 参数
        variables.put("message", "chenqiang message Test!!!");
        // 流程启动
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process",variables);
        TaskService taskService = activitiRule.getTaskService();
        // 获取任务
        Task task = taskService.createTaskQuery().singleResult();
        // 创建附件
        taskService.createAttachment("url",
                task.getId(),
                processInstance.getId(),
                "附件1",
                "描述11", "/test/test.jpg");
        // 获取附件
        List<Attachment> taskAttachments = taskService.getTaskAttachments(task.getId());
        for (Attachment attachment : taskAttachments) {
            logger.info("attachment = {}", ToStringBuilder.reflectionToString(attachment, ToStringStyle.JSON_STYLE));
        }
    }

    /**
     * 任务评论测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskComment() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        // message 参数
        variables.put("message", "chenqiang message Test!!!");
        // 流程启动
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process",variables);
        TaskService taskService = activitiRule.getTaskService();
        // 获取任务
        Task task = taskService.createTaskQuery().singleResult();
        // 创建评论
        taskService.addComment(task.getId(), processInstance.getId(), "评论测试一下！！！");

        // 获取评论
        List<Comment> taskAttachments = taskService.getTaskComments(task.getId());
        for (Comment comment : taskAttachments) {
            logger.info("comment = {}", ToStringBuilder.reflectionToString(comment, ToStringStyle.JSON_STYLE));
        }
    }



}
