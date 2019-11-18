package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
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
import java.util.Properties;

/**
 * 表单服务 formService
 */
public class CQ_FormServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(FormServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"cq-process-form.bpmn20.xml"})
    public void testFormService() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        FormService formService = activitiRule.getFormService();
        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        logger.info("formData = {}", ToStringBuilder.reflectionToString(startFormData, ToStringStyle.JSON_STYLE));

        List<FormProperty> formProperties = startFormData.getFormProperties();
        for (FormProperty formProperty : formProperties) {
            logger.info("formProperty = {}", ToStringBuilder.reflectionToString(formProperty,ToStringStyle.JSON_STYLE));
        }

        Map<String, String> properties = Maps.newHashMap();
        properties.put("message", "cq test message");
        // 提交开始表单
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), properties);

        Task task = activitiRule.getTaskService().createTaskQuery().singleResult();


        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        logger.info("taskFormData = {}", ToStringBuilder.reflectionToString(taskFormData, ToStringStyle.JSON_STYLE));

        List<FormProperty> taskFormDataFormProperties = taskFormData.getFormProperties();
        for (FormProperty formProperty : taskFormDataFormProperties) {
            logger.info("formProperty = {}", ToStringBuilder.reflectionToString(formProperty, ToStringStyle.JSON_STYLE));
        }

        // 提交用户表单
        Map<String, String> properties1 = Maps.newHashMap();
        properties1.put("yesORno", "yes");
        formService.submitTaskFormData(task.getId(), properties1);

        // 提交表单后判断任务是否还存在
        Task task1 = activitiRule.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
        logger.info("task1 = {}", task1);

    }


}
