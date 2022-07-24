console.log("This is Script file")


const toggleSidebar = () =>{
	var sidebar = document.getElementsByClassName("sidebar")[0];
	var data = document.getElementsByClassName("data")[0];
	if(sidebar.style.display === "block"){
		sidebar.style.display="none";
		data.style.marginLeft = "0%";
	}
	else{
		sidebar.style.display="block";
		data.style.marginLeft = "20%";
	}
}


function deleteContact(cid){
	swal({
		  title: "Are you sure?",
		  text: "You Want to Delete This Contact?",
		  icon: "warning",
		  buttons: true,
		  dangerMode: true,
		})
		.then((willDelete) => {
		  if (willDelete) {
		    window.location="/user/delete/"+cid
		  } else {
		    swal("Your Contact is safe!");
		  }
		});
}

const search = () =>{
	let query = document.getElementById("search-input").value;
	let searchResult = document.getElementsByClassName("search-result")[0];
	if(query === ""){
		searchResult.style.display = "none";
	}else{
		
		let url = `http://localhost:8081/search/${query}`;
		fetch(url).then((response)=> {
			return response.json();
		}).then((data)=>{
			let text = ``;
			//let final = ``;
			
			data.forEach((contact)=>{
				text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name} </a>`;
				
				
				
			})
				
				
				
			
			searchResult.innerHTML = "<div class='list-group'>" + text + "</div>";
			searchResult.style.display = "block";
		})
		
	}
}

const paymentStart=()=>{
	let paymentVal = document.getElementById("payment-input").value;
	if(paymentVal === "" || paymentVal===null){
		alert("Amount Is Required")
	}
	
	$.ajax(
	{
		url:'/user/create_order',
		data:JSON.stringify({amount:paymentVal,info:'order_request'}),
		contentType:'application/json',
		type:'POST',
		dataType:'json',
		success:function(response){
			console.log(response)
			if(response.status === "created"){
				let options={
						key:'rzp_test_k0H2CLGjkqMLCH',
						amount:response.amount,
						currency:'INR',
						name:'Smart Contact Manager',
						description:'donation',
						image:'https://www.linkedin.com/in/ranjit-panda-713781131/',
						order_id:response.id,
						handler:function(response){
							console.log(response.razorpay_payment_id,response.razorpay_order_id,response.razorpay_signature)
							alert('Payment Successfull!')
							
						},
						prefill: {
					        name: "Ranjit Panda",
					        email: "ranjitpanda224@gmail.com",
					        contact: "7894037398"
					    },
					    notes: {
					        address: "Smart Contact Manager- Ranjit Panda"
					    },
					    theme: {
					        color: "#3399cc"
					    }
						
				}
				
				let rzp = new Razorpay(options)
				rzp.on('payment.failed', function (response){
					console.log(response.error.code);
			        console.log(response.error.description);
			        console.log(response.error.source);
			        console.log(response.error.step);
			        console.log(response.error.reason);
			        console.log(response.error.metadata.order_id);
			        console.log(response.error.metadata.payment_id);
			        alert('payment failed !!!')
			});
				rzp.open()
			}
		},
		error:function(error){
			console.log(error)
			alert("Something Went Wrong!")
		}
	}		
	)
}
