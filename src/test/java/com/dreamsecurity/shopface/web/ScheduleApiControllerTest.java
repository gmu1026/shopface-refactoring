package com.dreamsecurity.shopface.web;
import com.dreamsecurity.shopface.domain.*;
import com.dreamsecurity.shopface.dto.schedule.ScheduleAddRequestDto;
import com.dreamsecurity.shopface.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.jni.Local;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ScheduleApiControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    EmployRepository employRepository;

    @Autowired
    OccupationRepository occupationRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Before
    public void setUp() {
        System.out.println("------------------------------------@Before");
        try {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                    .apply(documentationConfiguration(this.restDocumentation))
                    .build();

            // Member business
            Member business = Member.builder()
                    .id("test")
                    .password("1234")
                    .name("김사업")
                    .phone("01012341234")
                    .state("A")
                    .type("B")
                    .build();

            // Branch CU?
            Branch branch = Branch.builder()
                    .name("CU서울점")
                    .phone("01012341234")
                    .address("서울")
                    .detailAddress("강남")
                    .zipCode("11111")
                    .member(business)
                    .state("Y")
                    .build();

            // Member employee
            Member employee = Member.builder()
                    .id("emp01")
                    .password("1234")
                    .name("박알바")
                    .phone("01012341234")
                    .state("A")
                    .type("E")
                    .build();

            Role role = Role.builder().name("대리").branch(branch).build();
            Department department = Department.builder().name("인사").branch(branch).build();

            // Occupation cleaning
            Occupation occuapation = Occupation.builder()
                    .name("청소")
                    .branch(branch)
                    .build();

            // Employ employ (Cu에 employee가)
            Employ employ = Employ.builder().name("박알바").branch(branch).build();

            memberRepository.save(business);
            branchRepository.save(branch);
            occupationRepository.save(occuapation);
            departmentRepository.save(department);
            roleRepository.save(role);
            memberRepository.save(employee);
            employRepository.save(employ);
            employ.update(9000, role, department);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
        scheduleRepository.deleteAll();
        employRepository.deleteAll();
        roleRepository.deleteAll();
        departmentRepository.deleteAll();
        occupationRepository.deleteAll();
        branchRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    public void getScheduleList() {
    }

    @Test
    public void getSchedule() {
    }

    @Test
    public void addSchedule() throws Exception{
        //given
        Member member = memberRepository.findAll().get(0);
        Branch branch = branchRepository.findAll().get(0);
        Occupation occupation = occupationRepository.findAll().get(0);

        LocalDateTime workStartTime = LocalDateTime.of(2020, 8, 10, 0, 0, 0);
        LocalDateTime workEndTime = LocalDateTime.of(2020, 8, 10, 5, 0 , 0);
        String color = "#0070C0";

        Schedule schedule = Schedule.builder()
                .member(null)
                .branch(branch)
                .occupation(occupation)
                .color(color)
                .workStartTime(workStartTime)
                .workEndTime(workEndTime)
                .build();

        ScheduleAddRequestDto requestDto = new ScheduleAddRequestDto(schedule);
        String content = objectMapper.writeValueAsString(requestDto);
        //when
        mockMvc.perform(post("/schedule").content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("Schedule-add"));

        //then
        List<Schedule> results = scheduleRepository.findAll();
        assertThat(results.get(0).getMember().getId()).isEqualTo(member.getId());
        assertThat(results.get(0).getBranch().getNo()).isEqualTo(branch.getNo());
        assertThat(results.get(0).getWorkStartTime()).isEqualTo(workStartTime);
        assertThat(results.get(0).getWorkEndTime()).isEqualTo(workEndTime);
        assertThat(results.get(0).getOccupation().getNo()).isEqualTo(occupation.getNo());
        assertThat(results.get(0).getColor()).isEqualTo(color);
        assertThat(results.get(0).getState()).isEqualTo("R");
    }

    @Test
    public void editSchedule() {
    }

    @Test
    public void removeSchedule() {
    }
}