package com.cheersai.nexus.user.service.impl;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.user.mapper.UserMapper;
import com.cheersai.nexus.user.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cheersai.nexus.common.model.usermanagement.table.UserTableDef.USER;

/**
 * @ClassName:UserServiceImpl
 * @Description:用户管理Service
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:57
 */

@Service
@Component
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 查看所有用户
     * @param pageNumber 当前页码
     * @param pageSize 单页需要显示的用户数量
     * @param totalRow 非必须值（默认值为0，即全量查询），若传入该值，则不再去查询总数据量，可提高查询速度
     * @return Page<User>, 包含List<User> records(记录列表)、int pageNumber(当前页数)、int pageSize(当前页大小)、long totalPage(共计页数)、long totalRow(共计行数)
     */
    public Page<User> getAllUsers(int pageNumber, int pageSize, int totalRow) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(USER);
        if (totalRow > 0) {
            return userMapper.paginate(pageNumber, pageSize, totalRow, wrapper);
        }
        return userMapper.paginate(pageNumber, pageSize, wrapper);
    }
    
    public List<User> searchUsers(String userCondition) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(USER)
                .where(USER.USER_ID.like(userCondition))
                .or(USER.EMAIL.like(userCondition))
                .or(USER.PHONE.like(userCondition))
                .or(USER.USERNAME.like(userCondition))
                .or(USER.NICKNAME.like(userCondition))
                .or(USER.PASSWORD_HASH.like(userCondition))
                .or(USER.STATUS.eq(userCondition))
                .or(USER.EMAIL_VERIFIED.eq(userCondition))
                .or(USER.PHONE_VERIFIED.eq(userCondition))
                .or(USER.LAST_LOGIN_AT.like(userCondition))
                .or(USER.LAST_LOGIN_IP.like(userCondition))
                .or(USER.CREATED_AT.like(userCondition))
                .or(USER.UPDATED_AT.like(userCondition));
        return userMapper.selectListByQuery(wrapper);
    }
    
    public User getUserInfoById(String userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(USER)
                .where(USER.USER_ID.eq(userId));
        return userMapper.selectOneByQuery(wrapper);
    }
    
    public User updateUserStatus(String userId, String status) {
        User user = UpdateEntity.of(User.class, userId);
        user.setStatus(status);
        int count = userMapper.update(user);
        if (count < 1) {
            return null;
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(USER)
                .where(USER.USER_ID.eq(userId));
        return userMapper.selectOneByQuery(wrapper);
    }

}
