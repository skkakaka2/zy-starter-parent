package com.zy.mybatisplus;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zy.mybatisplus.audit.AuditUserContext;
import com.zy.mybatisplus.entity.BaseEntity;
import com.zy.mybatisplus.mapper.BaseMapperPlus;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = MybatisPlusIntegrationTest.TestApplication.class)
class MybatisPlusIntegrationTest {

    @Autowired
    private TestAccountMapper accountMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM test_account");
        AuditUserContext.clear();
    }

    @AfterEach
    void clearAuditUser() {
        AuditUserContext.clear();
    }

    @Test
    void insertFillsAuditFieldsAndInitializesCommonColumns() {
        TestAccount account = insertAccount("audit");

        assertThat(account.getId()).isNotNull();
        TestAccount stored = accountMapper.selectById(account.getId());
        assertThat(stored).isNotNull();
        assertThat(stored.getCreateBy()).isEqualTo(AuditUserContext.SYSTEM_USER_ID);
        assertThat(stored.getUpdateBy()).isEqualTo(AuditUserContext.SYSTEM_USER_ID);
        assertThat(stored.getCreateTime()).isNotNull();
        assertThat(stored.getUpdateTime()).isNotNull();
        assertThat(stored.getDeleted()).isZero();
        assertThat(stored.getVersion()).isZero();
    }

    @Test
    void pageReturnsCorrectTotal() {
        insertAccount("one");
        insertAccount("two");
        insertAccount("three");

        Page<TestAccount> page = accountMapper.selectPage(new Page<>(1, 2), null);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getRecords()).hasSize(2);
    }

    @Test
    void deleteByIdMarksRowAsDeleted() {
        TestAccount account = insertAccount("deleted");

        assertThat(accountMapper.deleteById(account.getId())).isEqualTo(1);
        assertThat(accountMapper.selectById(account.getId())).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT deleted FROM test_account WHERE id = ?", Integer.class, account.getId()))
                .isEqualTo(1);
    }

    @Test
    void updateByIdUsesOptimisticLock() {
        TestAccount account = insertAccount("before");
        TestAccount stored = accountMapper.selectById(account.getId());
        stored.setName("after");

        assertThat(accountMapper.updateById(stored)).isEqualTo(1);
        assertThat(stored.getVersion()).isEqualTo(1);
        assertThat(accountMapper.selectById(account.getId()).getVersion()).isEqualTo(1);
    }

    @Test
    void updateByIdRefreshesAuditUserForLoadedEntity() {
        TestAccount account = insertAccount("before");
        TestAccount stored = accountMapper.selectById(account.getId());
        AuditUserContext.setUserId(42L);
        stored.setName("after");

        assertThat(accountMapper.updateById(stored)).isEqualTo(1);
        assertThat(accountMapper.selectById(account.getId()).getUpdateBy()).isEqualTo(42L);
    }

    private TestAccount insertAccount(String name) {
        TestAccount account = new TestAccount();
        account.setName(name);
        assertThat(accountMapper.insert(account)).isEqualTo(1);
        return account;
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @Getter
    @Setter
    @TableName("test_account")
    public static class TestAccount extends BaseEntity {

        private String name;
    }

    @Mapper
    public interface TestAccountMapper extends BaseMapperPlus<TestAccount> {
    }
}
