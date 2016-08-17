package org.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyApp {

    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }

//    @Bean
//    public CommandLineRunner init(final RepositoryService repositoryService,
//                                  final RuntimeService runtimeService,
//                                  final TaskService taskService) {
//
//        return new CommandLineRunner() {
//            @Override
//            public void run(String... strings) throws Exception {
//                Map<String, Object> variables = new HashMap<String, Object>();
//                variables.put("applicantName", "John Doe");
//                variables.put("email", "john.doe@activiti.com");
//                variables.put("phoneNumber", "123456789");
//                runtimeService.startProcessInstanceByKey("hireProcess", variables);
//            }
//        };
//
//    }

    @Bean
    InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

        return new InitializingBean() {
            public void afterPropertiesSet() throws Exception {
                //建组
                Group agent = identityService.newGroup("agent");
                agent.setName("代理商户");
                agent.setType("security-role");
                identityService.saveGroup(agent);
                
                Group directSale = identityService.newGroup("directSale");
                directSale.setName("直营商户");
                directSale.setType("security-role");
                identityService.saveGroup(directSale);
                
                Group platExecutive = identityService.newGroup("platExecutive");
                platExecutive.setName("平台运营");
                platExecutive.setType("security-role");
                identityService.saveGroup(platExecutive);
                
                Group customer = identityService.newGroup("customer");
                customer.setName("客户");
                customer.setType("security-role");
                identityService.saveGroup(customer);
                //建用户
                User admin = identityService.newUser("admin");
                admin.setPassword("admin");
                admin.setFirstName("平台运营");
                identityService.saveUser(admin);

                User agentBelong = identityService.newUser("agent1");
                agentBelong.setPassword("123");
                agentBelong.setFirstName("代理1-推广者");
                identityService.saveUser(agentBelong);

                User anotherAgent = identityService.newUser("agent2");
                anotherAgent.setPassword("123");
                anotherAgent.setFirstName("代理2");
                identityService.saveUser(anotherAgent);

                User directAgent = identityService.newUser("agent3");
                directAgent.setPassword("123");
                directAgent.setFirstName("直营代理");
                identityService.saveUser(directAgent);

                User c1 = identityService.newUser("customer1");
                c1.setPassword("123");
                c1.setFirstName("客户1");
                identityService.saveUser(c1);

                User c2 = identityService.newUser("customer2");
                c2.setPassword("123");
                c2.setFirstName("客户2");
                identityService.saveUser(c2);
                //绑定用户组
                identityService.createMembership(admin.getId(),platExecutive.getId());//运营人员绑定
                identityService.createMembership(agentBelong.getId(),agent.getId());//代理商户绑定
                identityService.createMembership(anotherAgent.getId(),agent.getId());//代理商户绑定
                identityService.createMembership(directAgent.getId(),directSale.getId());//直营商户绑定
                identityService.createMembership(c1.getId(),customer.getId());//普通用户绑定
                identityService.createMembership(c2.getId(),customer.getId());//普通用户绑定


            }
        };
    }

}
