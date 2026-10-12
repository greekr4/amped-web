0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 380.0 , 380.0 , 380.0 , 4.5 , 0, 0.22) ;
	}
260 : {
		move(0,2.6);
		makeShapeModel("/effect/jet.oa",0,0.1,-0.4,0,0,-1,1);
		makeShapeModel("/effect/honoo.oa",0,0.1,0.5,0,0,1,1);
		startAnimation( 380.0 , 381.0 , 381.0 , 8.0 , 0) ;
	}
380 : {
		attack("rush");
		startAnimation( 381.0 , 383.0 , 381.0 , 24.0 , 1) ;
	}
650 : {
		attack("rush");
	}
900 : {
		fadeinAnimation( 384.0 , 384.0 , 384.0 , 3.0 , 0, 0.4) ;
		makeShapeModel("/effect/explode1.oa",0,0.2,0.6,0,0,1);
	}
1050 : {
		move(0,1.2);
	}
1500 : {
		startAnimation( 384.0 , 385.0 , 385.0 , 5.0 , 0) ;
	}
1700 : {
		recover();
	}
