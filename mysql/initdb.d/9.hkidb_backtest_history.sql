insert into hkidb.backtest_history (uid, aid, param, plratio)
values  (1, 1, '{"dca": [0.25, 0.25, 0.25, 0.25], "uid": "1", "title": "포스트맨예시", "tickers": ["000270", "000660", "003670", "005380", "005490"], "end_date": "20151231", "start_date": "20120101", "max_trading": "3", "buy_conditions": [[{"frequency": "1, 1", "compare_type": ">", "base_indicator": "MACD_SIGNAL, 12, 26, 9", "constant_bound": "0", "target_indicator": "MACD, 12, 26"}]], "initial_balance": "10000000", "sell_conditions": [[{"frequency": "1, 1", "compare_type": "<", "base_indicator": "MACD_SIGNAL, 12, 26, 9", "constant_bound": "0", "target_indicator": "MACD, 12, 26"}]]}', 0.2553316000000001);