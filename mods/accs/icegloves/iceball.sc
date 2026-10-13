0 : {
		stop();
		changeFace(1);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.2) ;
	}
250 : {
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
	}
320 : {
		makeShapeModel("/effect/cold1.oa",0,0.1,0.6,0,0,1);
		attack("iceball");
	}
600 : {
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
		nextTech();
	}
900 : {
		recover();
	}
