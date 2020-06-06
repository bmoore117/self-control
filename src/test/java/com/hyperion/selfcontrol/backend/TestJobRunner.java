package com.hyperion.selfcontrol.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;
import com.hyperion.selfcontrol.backend.config.job.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestJobRunner {

    public static final ObjectMapper mapper = ConfigService.mapper;

    public void testSerialization(Job job) throws JsonProcessingException {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(job.dehydrateJob());

        Job rehydrated = mapper.readValue(json, Job.class);
        Job reconstituted = JobRunner.convertIfNecessary(rehydrated);
        Assert.assertEquals(reconstituted.getConcreteClass(), reconstituted.getClass());
    }

    @Test
    public void addHost() throws JsonProcessingException {
        AddHostJob job = new AddHostJob(LocalDateTime.now(), "Add host job", "abc.com", true);
        testSerialization(job);
    }

    @Test
    public void removeHost() throws JsonProcessingException {
        RemoveHostJob job = new RemoveHostJob(LocalDateTime.now(), "Add host job", "abc.com", true);
        testSerialization(job);
    }

    @Test
    public void setDelay() throws JsonProcessingException {
        SetDelayJob job = new SetDelayJob(LocalDateTime.now(), "Add host job", 10L);
        testSerialization(job);
    }

    @Test
    public void deleteCustomFilter() throws JsonProcessingException {
        List<Keyword> keywordList = Arrays.asList(new Keyword("a"), new Keyword("b"), new Keyword("c"));
        CustomFilterCategory filter = new CustomFilterCategory("name", "status", keywordList);
        DeleteCustomFilterJob job = new DeleteCustomFilterJob(LocalDateTime.now(), "delete custom filter job", filter);
        testSerialization(job);
    }

    @Test
    public void toggleFilter() throws JsonProcessingException {
        List<Keyword> keywordList = Arrays.asList(new Keyword("a"), new Keyword("b"), new Keyword("c"));
        CustomFilterCategory filter = new CustomFilterCategory("name", "status", keywordList);
        FilterCategory filterCategory = new FilterCategory("name", "status");
        List<AbstractFilterCategory> filterCategories = Arrays.asList(filter, filterCategory);
        ToggleFilterJob job = new ToggleFilterJob(LocalDateTime.now(), "delete custom filter job", "menuItem", filterCategories);
        testSerialization(job);
    }

    @Test
    public void toggleSafeSearch() throws JsonProcessingException {
        ToggleSafeSearchJob job = new ToggleSafeSearchJob(LocalDateTime.now(), "delete custom filter job", true);
        testSerialization(job);
    }

    @Test
    public void updateBedtimes() throws JsonProcessingException {
        Bedtimes bedtimes = new Bedtimes();
        bedtimes.setFriday(LocalTime.now());
        bedtimes.setSaturday(LocalTime.MIDNIGHT);
        UpdateBedtimesJob job = new UpdateBedtimesJob(LocalDateTime.now(), "delete custom filter job", bedtimes);
        testSerialization(job);
    }

    @Test
    public void updateCustomFilter() throws JsonProcessingException {
        List<Keyword> keywordList = Arrays.asList(new Keyword("a"), new Keyword("b"), new Keyword("c"));
        CustomFilterCategory filter = new CustomFilterCategory("name", "status", keywordList);
        UpdateCustomFilterJob job = new UpdateCustomFilterJob(LocalDateTime.now(), "delete custom filter job", filter);
        testSerialization(job);
    }
}
