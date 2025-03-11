CPX #$0100
	BNE 03;x_not_100
	LDX #$0200
	;x_not_100:
	
LDA #$08
STA $76; loop counter

copy_bytes_bot_tiles_first_half:
	LDA [$6D],Y ; load bot tile bytes
	JSL $C30B90; jump to write bot tile
	
	INX
	INY
	DEC $76
	BNE copy_bytes_bot_tiles_first_half
	
LDA #$08
STA $76; loop counter

copy_bytes_first_half:
	LDA [$6A],Y ; load top tile bytes
	;STA $7EF000,X
	JSL $C30B80; jump to write top tile
	LDA [$6D],Y ; load bot tile bytes
	; STA $7EF100,X
	JSL $C30B90; jump to write bot tile
	INX
	INY
	DEC $76
	BNE copy_bytes_first_half
	RTL