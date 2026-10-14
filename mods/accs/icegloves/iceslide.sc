0 : {
		bind(1300);
		move(0,1.5);
		changeFace(2);
		fadeinAnimation( 335.0 , 335.0 , 335.0 , 4.5 , 0, 0.2) ;
		ignoreAttack(1000);
		makeParticle("cold.ptc",0.0,-0.2,0.3,0,1,0);
	}
250 : {
		move(0,4.0);
		makeShapeModel("/effect/frzflush.oa",0,-0.2,0.3,0,0,1,1);
		makeShapeModel("/effect/iceshot_hadou.oa",0,-0.1,0.6,0,0,1,1);
		startAnimation( 335.0 , 336.0 , 336.0 , 10.0 , 0) ;
	}
300 : {
		attack("slide1");
	}
550 : {
		makeShapeModel("/effect/iceshot_clash.oa",0,-0.1,0.5,0,0,1,1);
		attack("slide2");
	}
850 : {
		move(0,1.0);
		startAnimation( 336.0 , 337.0 , 337.0 , 6.0 , 0) ;
	}
1000 : {
		stop();
		startAnimation( 338.0 , 339.0 , 339.0 , 4.0 , 0) ;
	}
1300 : {
		recover();
	}
