package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查询参数组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return specGroupMapper.select(specGroup);
    }

    /**
     * 根据gid查询参数类型
     *
     * @param cid
     * @param gid
     * @param generic
     * @param searching
     * @return
     */
    @Override
    public List<SpecParam> queryParamByGid(Long gid,Long cid, Boolean generic, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return specParamMapper.select(specParam);
    }

    /**
     * 根据cid查询下面的组，和组下的参数
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupAndParamByCid(Long cid) {
        List<SpecGroup> groups = this.queryGroupByCid(cid);
        groups.forEach(group->{
            List<SpecParam> params = this.queryParamByGid(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }
}
