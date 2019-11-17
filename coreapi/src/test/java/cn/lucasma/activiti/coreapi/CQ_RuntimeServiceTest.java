package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;

/**
 * 流程运行runtimeService
 * @Date 2019/11/14 22:45
 * @Author chq
 */
public class CQ_RuntimeServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CQ_RuntimeServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();


    /**
     * 通过key启动流程，默认会启动最新版本
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void TestStartProcessByKey() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        // 添加变量
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key1", "value1");
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process", variables);
        logger.info("processInstance = {}", processInstance);
    }

    /**
     * 通过 流程 ID 启动
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void TestStartProcessById() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessDefinition processDefinition = activitiRule.getRepositoryService().createProcessDefinitionQuery().singleResult();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        logger.info("processInstance = {}", processInstance);
    }

    /**
     * 通过 ProcessInstanceBuilder 启动
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void TestStartProcessByBuilder() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key1", "value1");

        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .businessKey("businessKey001")
                .processDefinitionKey("my-process")
                .variables(variables)
                .start();

        logger.info("processInstance = {}", processInstance);
    }

    /**
     * 流程变量测试
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testVariables() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key1", "value1");
        variables.put("key2", "value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);
        logger.info("processInstance = {}", processInstance);
        logger.info("修改前：variables = {}", variables);
        // 添加新变量key3
        runtimeService.setVariable(processInstance.getId(), "key3", "value4");
        // 修改变量key2
        runtimeService.setVariable(processInstance.getId(), "key2", "value2_new");

        // 输出变量
        Map<String, Object> variables1 = runtimeService.getVariables(processInstance.getId());
        logger.info("修改后：variables1 = {}", variables1);

    }

    /**
     * 查询流程实例 runtimeService.createProcessInstanceQuery
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testProcessInstanceQuery() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
        logger.info("processInstance = {}", processInstance);

        ProcessInstance processInstance1 = runtimeService.createProcessInstanceQuery().singleResult();
        logger.info("processInstance1 = {}", processInstance1);
    }

    /**
     * 流程执行对象的查询
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testExecutionQuery() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        logger.info("processInstance = {}", processInstance);

        List<Execution> executionList = runtimeService.createExecutionQuery().listPage(0, 100);
        for (Execution execution : executionList) {
            logger.info("execution = {}", execution);
        }
    }

    /**
     * processInstance（流程实例）和execution（执行器）区别
     *  ProcessInstance 表示一次工作流业务的实体，当每次启动流程的时候，生成一个流程实例
     *  Execution 表示流程实例中具体的执行路径，如果是说一个简单的流程，其只有一条线的话，可以理解为，每一次流程实例就对应一个执行流
     *  这种情况下流程实例和执行流对应的 ID 是一致的。
     *  ProcessInstance 继承 Execution
     */

    /**
     * receiveTask
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-trigger.bpmn20.xml"})
    public void testTrigger(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        // 启动流程
        runtimeService.startProcessInstanceByKey("my-process");
        Execution execution = runtimeService.createExecutionQuery()
                .activityId("someTask").singleResult();
        logger.info("execution = {}", execution);

        runtimeService.trigger(execution.getId());
        execution = runtimeService.createExecutionQuery()
                .activityId("some-task").singleResult();
        logger.info("execution = {}", execution);
    }

    /**
     *  信号触发
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-signal-received.bpmn20.xml"})
    public void testSignalEventReceived(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
        Execution execution = runtimeService.createExecutionQuery()
                .signalEventSubscriptionName("my-signal").singleResult();
        logger.info("execution = {}", execution);
        // 信号触发
        runtimeService.signalEventReceived("my-signal");
        execution = runtimeService.createExecutionQuery()
                .signalEventSubscriptionName("my-signal").singleResult();
        logger.info("execution = {}", execution);
    }

    /**
     * 消息 触发
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-message-received.bpmn20.xml"})
    public void testMessageEventReceived(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();
        logger.info("execution = {}", execution);

        runtimeService.messageEventReceived("my-message", execution.getId());
        execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();
        logger.info("execution = {}", execution);
    }

    /**
     * message 启动流程
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-message.bpmn20.xml"})
    public void testMessageStart() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        // 其实最终也是通过startProcessInstanceByKey启动的，只是多了一个根据message找到key的步骤
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByMessage("my-message");
        logger.info("processInstance = {}", processInstance);
    }
}
