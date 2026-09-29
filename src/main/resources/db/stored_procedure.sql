DROP PROCEDURE IF EXISTS sp_get_portfolio_value;
CREATE PROCEDURE sp_get_portfolio_value(IN p_user_id BIGINT, OUT p_total_value DECIMAL(20,8))
BEGIN
    SELECT IFNULL(SUM(w.balance * a.current_price_usd), 0)
    INTO   p_total_value
    FROM   wallets w
    JOIN   assets  a ON w.currency_code = a.code
    WHERE  w.user_id = p_user_id
      AND  w.currency_code <> 'USD';
END
