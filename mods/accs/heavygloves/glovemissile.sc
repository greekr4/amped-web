0 : {
		move(0,1.2);
		changeFace(2);
		fadeinAnimation( 245.0 , 245.0 , 245.0 , 4.5 , 0, 0.2) ;
	}
220 : {
		stop();
		startAnimation( 245.0 , 246.0 , 246.0 , 6.0 , 0) ;
	}
340 : {
		attack("glovewave");
		startAnimation( 246.0 , 248.0 , 246.0 , 24.0 , 1) ;
	}
700 : {
		nextTech();
	}
1000 : {
		recover();
	}
