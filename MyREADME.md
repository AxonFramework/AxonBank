# 项目结构
1. CORE--包含聚合根、Command Handler处理和Saga事件管理
    1. 聚合根
    2. Command Handler
    3. sagag管理事件
2. CORE-API--包含所有命令及事件的字段处理
    1. 事件
    2. command
3. CORE-QUERY--包含Entity、Repository和事件监听
    1. Entity
    2. 事件监听
    3. JPARepository
4. WEB--包含配置和API[websocket]
    1. CommandHandler配置
    2. Saga配置
    3. Interceptors配置