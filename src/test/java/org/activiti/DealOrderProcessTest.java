package org.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.subethamail.wiser.Wiser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Jason on 16/8/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {MyApp.class})
@WebAppConfiguration
@IntegrationTest
public class DealOrderProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private LsrsOrderRepository lsrsOrderRepository;

    private Wiser wiser;

    @Before
    public void setup() {
        wiser = new Wiser();
        wiser.setPort(1025);
        wiser.start();
    }

    @After
    public void cleanup() {
        wiser.stop();
    }

@Test
    public void testOrderDealProcess(){
        //下订单并启动流程,流程id存于订单中备用,订单状态:0未分配,1已接单,2施工中,3完工,4,关闭
        //初始化变量
        Random rd = new Random();
        Long userId = rd.nextLong();
        Double price = rd.nextDouble();
        String belongs = rd.nextBoolean()?"agent1":"other";
        //启动流程
        Map<String, Object> variables = new HashMap<String, Object>();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("orderDealProcess", variables);
        //用户下单
        LsrsOrder order = new LsrsOrder(userId,"testUser","131111111111","钓鱼台",belongs,123,price,0,processInstance.getId());
        lsrsOrderRepository.save(order);
        //分配订单任务
        Task orderTask = taskService.createTaskQuery()
                .processInstanceId(order.getProcessId())
//                .taskCandidateGroup("customer")
                .singleResult();

        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("order", order);
        taskVariables.put("parentId","agent1");
        taskVariables.put("agentId","agent1");
        taskService.complete(orderTask.getId(),taskVariables);

        //判断第一次分支
        Task task1 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent1").singleResult();
        if(task1!=null){//有推广码的分支
            agreeOrder(task1,order,"agent1",1);

            Task task11 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent1").singleResult();
            confirmOrder(task11,order,"agent1",0);

//            Task task12 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskCandidateGroup("platExecutive").singleResult();
//            deliverOrder(task12,order,"agent3");
//
//            Task task13 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent3").singleResult();
//            confirmOrder(task13,order,"agent3",1);

//            Task task14 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent3").singleResult();
//            orderForm(task14,order,"agent3","order full dealed!");


        }

        Task task2 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskCandidateGroup("platExecutive").singleResult();
        if(task2!=null){//无推广码的分支
            deliverOrder(task2,order,"agent2");//管理员分配订单
            Task task21 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent2").singleResult();
            agreeOrder(task21,order,"agent2",1);//被分配代理确认接单
            Task task22 = taskService.createTaskQuery().processInstanceId(order.getProcessId()).taskAssignee("agent2").singleResult();
            confirmOrder(task22,order,"agent2",0);

        }



        Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    }


    private void deliverOrder(Task task ,LsrsOrder order,String name){
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("agentId",name);
        taskVariables.put("order",order);
        taskService.complete(task.getId(),taskVariables);
        System.out.println(task.getName()+"::"+taskVariables.toString()+"::orderState-"+order.getOrderState());
    }

    //接单
    private void agreeOrder(Task task ,LsrsOrder order, String name,Integer result){
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        order.setOrderState(1);
        taskVariables.put("agree",result);
        taskVariables.put("agentId",name);
        taskVariables.put("order",order);
        taskService.complete(task.getId(),taskVariables);
        System.out.println(task.getName()+"::"+taskVariables.toString()+"::orderState-"+order.getOrderState());
    }
    //确认订单
    private void confirmOrder(Task task ,LsrsOrder order, String name,Integer result){
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        order.setOrderState(2);
        taskVariables.put("confirm",result);
        taskVariables.put("agentId",name);
        taskVariables.put("order",order);
        taskService.complete(task.getId(),taskVariables);
        System.out.println(task.getName()+"::"+taskVariables.toString()+"::orderState-"+order.getOrderState());
    }

    private void orderForm(Task task,LsrsOrder order,String name,String desc){
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        order.setOrderState(3);
        taskVariables.put("order",order);
        taskService.complete(task.getId(),taskVariables);
        System.out.println(task.getName()+"::"+taskVariables.toString()+"::orderState-"+order.getOrderState());
    }

}
