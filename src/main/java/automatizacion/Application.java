package automatizacion;

import automatizacion.services.IStorageService;
import automatizacion.task.ProcessFilesTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Application
 * 
 * @author David Martos Grande
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private static final Logger log = LogManager.getLogger(Application.class);
    
    @Autowired
    private SchedulerFactoryBean sfb;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    CommandLineRunner init(IStorageService storageService) {
        return (args) -> {
            log.info("Application started");
            storageService.init();
            Scheduler scheduler = sfb.getScheduler();

            JobDetail job = JobBuilder.newJob(ProcessFilesTask.class)
                    .withIdentity("job1", "group1")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(cronSchedule("0 0 3 1/1 * ? *")) //Every day at 3am
                    .build();

            scheduler.scheduleJob(job, trigger);
        };
    }
}
