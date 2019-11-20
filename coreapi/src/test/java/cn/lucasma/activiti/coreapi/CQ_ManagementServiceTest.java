package cn.lucasma.activiti.coreapi;

import cn.lucasma.activiti.mapper.MyCustomMapper;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.runtime.*;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 管理服务 managementService
 */
public class CQ_ManagementServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CQ_ManagementServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("cq_activiti_job.cfg.xml");

    /**
     * job查询
     */
    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testJobQuery() {
        ManagementService managementService = activitiRule.getManagementService();
        List<Job> jobList = managementService.createTimerJobQuery().listPage(0, 100);
        for (Job timerJob : jobList) {
            logger.info("timerJob = {}", timerJob);
        }
        JobQuery jobQuery = managementService.createJobQuery();
        SuspendedJobQuery suspendedJobQuery = managementService.createSuspendedJobQuery();
        DeadLetterJobQuery deadLetterJobQuery = managementService.createDeadLetterJobQuery();
        List<Job> jobs = jobQuery.listPage(0, 100);
        for (Job job : jobList) {
            logger.info("job = {}", job);
        }
    }

    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testTablePageQuery() {
        ManagementService managementService = activitiRule.getManagementService();
        TablePage tablePage = managementService.createTablePageQuery()
                .tableName(managementService.getTableName(ProcessDefinitionEntity.class))
                .listPage(0, 100);
        List<Map<String, Object>> rows = tablePage.getRows();
        for (Map row : rows) {
            logger.info("row = {}", row);
        }
    }

    /**
     * 测试自定义sql查询
     */
    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testCustomSql() {
        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        ManagementService managementService = activitiRule.getManagementService();

        List<Map<String, Object>> mapList = managementService.executeCustomSql(new AbstractCustomSqlExecution<MyCustomMapper, List<Map<String, Object>>>(MyCustomMapper.class) {
            @Override
            public List<Map<String, Object>> execute(MyCustomMapper myCustomMapper) {
                return myCustomMapper.findAll();
            }
        });
        for (Map map : mapList) {
            logger.info("map = {}", map);
        }
    }

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testCommand() {
        activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        ManagementService managementService = activitiRule.getManagementService();

        ProcessDefinitionEntity processDefinitionEntity = managementService.executeCommand(new Command<ProcessDefinitionEntity>() {
            @Override
            public ProcessDefinitionEntity execute(CommandContext commandContext) {
                ProcessDefinitionEntity entity = commandContext.getProcessDefinitionEntityManager()
                        .findLatestProcessDefinitionByKey("my-process");
                return entity;
            }
        });
        logger.info("processDefinitionEntity = ", processDefinitionEntity);

    }
}
