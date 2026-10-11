DROP TABLE IF EXISTS redemption;
DROP TABLE IF EXISTS voucher;
DROP TABLE IF EXISTS campaign;

CREATE TABLE campaign (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    client_code     VARCHAR(50)  NOT NULL,
    total_stock     INT          NOT NULL,
    remaining_stock INT          NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    user_voucher_limit INT NOT NULL DEFAULT 2
);

CREATE TABLE voucher (
    id          BIGINT PRIMARY KEY,
    campaign_id BIGINT       NOT NULL,
    code        VARCHAR(40)  NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    redeemed_by VARCHAR(80),
    redeemed_at TIMESTAMP
);

CREATE TABLE redemption (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id  BIGINT      NOT NULL,
    campaign_id BIGINT      NOT NULL,
    user_id     VARCHAR(80) NOT NULL,
    created_at  TIMESTAMP   NOT NULL
);

CREATE TABLE user_campaign_quota (
    user_id     VARCHAR(80) NOT NULL,
    campaign_id BIGINT      NOT NULL,
    used_count  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, campaign_id)
);
