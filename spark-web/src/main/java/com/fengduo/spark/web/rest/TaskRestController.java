/*
 * Copyright 2015-2020 MSUN.com All right reserved. This software is the confidential and proprietary information of
 * MSUN.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with MSUN.com.
 */
package com.fengduo.spark.web.rest;

import java.net.URI;
import java.util.List;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fengduo.spark.commons.validator.BeanValidators;
import com.fengduo.spark.model.entity.Task;
import com.fengduo.spark.service.interfaces.TaskService;
import com.fengduo.spark.web.cons.MediaTypes;

/**
 * Task的Restful API的Controller.
 * 
 * @author zxc
 */
@RestController
@RequestMapping(value = "/api/v1/task")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TaskRestController {

    private static Logger logger = LoggerFactory.getLogger(TaskRestController.class);

    @Autowired
    private TaskService   taskService;

    @Autowired
    private Validator     validator;

    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public List<Task> list() {
        return taskService.getAllTask();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaTypes.JSON_UTF_8)
    public Task get(@PathVariable("id")
    Long id) {
        Task task = taskService.getTask(id);
        if (task == null) {
            String message = "任务不存在(id:" + id + ")";
            logger.warn(message);
            throw new RestException(HttpStatus.NOT_FOUND, message);
        }
        return task;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaTypes.JSON)
    public ResponseEntity<?> create(@RequestBody
    Task task, UriComponentsBuilder uriBuilder) {
        // 调用JSR303 Bean Validator进行校验, 异常将由RestExceptionHandler统一处理.
        BeanValidators.validateWithException(validator, task);

        // 保存任务
        taskService.saveTask(task);

        // 按照Restful风格约定，创建指向新任务的url, 也可以直接返回id或对象.
        Long id = task.getId();
        URI uri = uriBuilder.path("/api/v1/task/" + id).build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaTypes.JSON)
    // 按Restful风格约定，返回204状态码, 无内容. 也可以返回200状态码.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody
    Task task) {
        // 调用JSR303 Bean Validator进行校验, 异常将由RestExceptionHandler统一处理.
        BeanValidators.validateWithException(validator, task);

        // 保存任务
        taskService.saveTask(task);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id")
    Long id) {
        taskService.deleteTask(id);
    }
}
