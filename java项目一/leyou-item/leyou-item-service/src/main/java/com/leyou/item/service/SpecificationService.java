package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> queryGroupByCid(Long cid);

    List<SpecParam> queryParamByGid(Long cid, Long gid, Boolean generic, Boolean searching);

    List<SpecGroup> queryGroupAndParamByCid(Long cid);
}
