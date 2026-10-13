0 : {
		move(0,1.2);
		changeFace(1);
		fadeinAnimation( 245.0 , 245.0 , 245.0 , 4.5 , 0, 0.2) ;
	}
220 : {
		stop();
		startAnimation( 245.0 , 246.0 , 246.0 , 6.0 , 0) ;
	}
340 : {
		makeShapeModel("/effect/cold1.oa",0.2,-0.1,-0.5,0,1,0);
		attack("iceshot");
		startAnimation( 246.0 , 248.0 , 246.0 , 24.0 , 1) ;
	}
700 : {
		nextTech();
	}
1000 : {
		recover();
	}
