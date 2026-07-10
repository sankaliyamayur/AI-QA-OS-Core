package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.database")
public class DatabaseProperties {
    private String dialect = "org.hibernate.dialect.PostgreSQLDialect";
    private boolean showSql = false;
    private boolean formatSql = false;

    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }
    public boolean isShowSql() { return showSql; }
    public void setShowSql(boolean showSql) { this.showSql = showSql; }
    public boolean isFormatSql() { return formatSql; }
    public void setFormatSql(boolean formatSql) { this.formatSql = formatSql; }
}