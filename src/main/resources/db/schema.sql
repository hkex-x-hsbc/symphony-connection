create sequence sq_margin_call_record increment by 1 start with 1 nocache ;

create table t_margin_call_record
(
    CALL_ID NUMBER NOT NULL,
    CALL_TIME DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CLEARING_PARTICIPANT_ID VARCHAR2(256),
    EXCHANGE_PARTICIPANT_ID VARCHAR2(256),
    IDM_CALL_TYPE VARCHAR2(16) NOT NULL,
    PAYMENT_CURRENCY VARCHAR2(8) NOT NULL,
    PAYMENT_AMOUNT NUMBER NOT NULL,
    MARKET_CURRENCY VARCHAR2(8) NOT NULL,
    MARKET_AMOUNT NUMBER NOT NULL,
    STATUS VARCHAR2(32) NOT NULL,
    constraint pk_margin_call_record primary key (CALL_ID)
);

create table t_idm_room_mapping_c
(
    STOCK_CODE VARCHAR2(256) NOT NULL,
    SYMPHONY_ID VARCHAR2(128) NOT NULL
);

create table t_idm_room_mapping_d
(
    SYMPHONY_ID VARCHAR2(128) NOT NULL
);