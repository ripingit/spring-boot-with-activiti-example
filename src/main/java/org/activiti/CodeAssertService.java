package org.activiti;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Created by Jason on 16/8/16.
 */
@Service
public class CodeAssertService {

    public String hasParentCode(String customer){
        Random rd = new Random();
        return rd.nextBoolean()?"":"-1";
    }
}
