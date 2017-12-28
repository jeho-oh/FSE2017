H264 : no_asm [no_8x8dct] [no_cabac] [no_deblock] [no_fast_pskip] [no_mbtree] [no_mixed_refs] [no_weightb] rc_lookahead ref  :: _H264 ;
rc_lookahead : rc_lookahead_20 | rc_lookahead_40 | rc_lookahead_60 :: _rc_lookahead ;
ref : ref_1 | ref_5 | ref_9 :: _ref ;