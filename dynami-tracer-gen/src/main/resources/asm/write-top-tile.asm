; This function acts as a STA DP Indirect Long Indexed, X 
; STA $7EF000,X
; 

PHP
STY $77
TXY
STA [$60],Y
LDY $77
PLP
RTL