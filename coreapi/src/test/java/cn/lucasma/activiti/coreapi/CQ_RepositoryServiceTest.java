package cn.lucasma.activiti.coreapi;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 流程存储服务repositoryService
 * @Date 2019/11/14 22:45
 * @Author chq
 */
public class CQ_RepositoryServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CQ_RepositoryServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void testRepository() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();

        // 部署流程
        deploymentBuilder.name("测试部署资源1")
                .addClasspathResource("my-process.bpmn20.xml")
                .addClasspathResource("second_approve.bpmn20.xml");
        // deploy的时候可以把静态资源放到数据库里面
        Deployment deploy = deploymentBuilder.deploy();
        logger.info("deploy = {}", deploy);

        // 再次部署
        DeploymentBuilder deploymentBuilder1 = repositoryService.createDeployment().name("测试部署资源2")
                .addClasspathResource("my-process.bpmn20.xml")
                .addClasspathResource("second_approve.bpmn20.xml");
        Deployment deploy1 = deploymentBuilder1.deploy();

        // 查询部署对象
        List<Deployment> deploymentList = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime().asc().listPage(0, 100);

        for (Deployment deployment : deploymentList) {
            logger.info("流程部署对象，deploy = {}", deployment);
        }
        logger.info("deploymentList.size:{}", deploymentList.size());

        // 查询流程定义
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .listPage(0, 100);
        for (ProcessDefinition processDefinition : processDefinitionList) {
            logger.info("id = {}, name = {}, key = {}, version = {}", processDefinition.getId(),
                    processDefinition.getName(),
                    processDefinition.getKey(),
                    processDefinition.getVersion());
        }

    }

    /**
     * 测试暂停流程定义文件
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testSuspend() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        logger.info("processDefinition.id = {}", processDefinition.getId());

        //暂停流程定义
        repositoryService.suspendProcessDefinitionById(processDefinition.getId());

        //暂停后启动
        try {
            logger.info("开始启动");
            activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
            logger.info("启动成功");
        } catch (Exception e) {
            logger.error("启动失败", e);
        }

        // 激活重新启动
        repositoryService.activateProcessDefinitionById(processDefinition.getId());
        logger.info("重新启动");
        activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        logger.info("重新启动成功");
    }

}
