
var LunchSelector = React.createClass({
	getInitialState: function(){
		return({
			currentVotes: [],
			popularRestaurants: [],
			restaurantList: []
		});
	},
	getCurrentVotes: function(){
		$.ajax({
      url: "/api/current-votes",
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({currentVotes: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error("/api/current-votes", status, err.toString());
      }.bind(this)
    });
	},
	getPopularRestaurants: function(){
		$.ajax({
      url: "/api/popular-restaurant",
      dataType: 'json',
      cache: false,
      success: function(data) {
				this.setState({popularRestaurants: data});
      }.bind(this),
      error: function(xhr, status, err) {
				console.error("/api/popular-restaurant", status, err.toString());
      }.bind(this)
    });
	},
	getRestaurantList: function(){
		$.ajax({
      url: "/api/restaurants",
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({restaurantList: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error("/api/restaurants", status, err.toString());
      }.bind(this)
    });
	},
	handleSubmitVotes: function(){
		this.getCurrentVotes();
		this.getPopularRestaurants();
	},
	componentDidMount: function(){
		this.getCurrentVotes();
		this.getPopularRestaurants();
		this.getRestaurantList();
	},
	render: function(){
		return(
			<div className="container">
				<div className="row">
					<div className="col-md-6">
						<RestaurantList data={this.state.currentVotes} type="Current Votes" onSubmitHandle={this.handleSubmitVotes} />
					</div>
					<div className="col-md-6">
						<RestaurantList data={this.state.popularRestaurants} type="Popular Restaurants" onSubmitHandle={this.handleSubmitVotes} />
					</div>
				</div>
				<div className="row">
					<div className="col-md-6">
						<RestaurantList data={this.state.restaurantList} type="Restaurant List" onSubmitHandle={this.handleSubmitVotes} />
					</div>
				</div>
			</div>
		);
	}
});

var RestaurantList = React.createClass({
	getDefaultProps: function() {
    return {
      data: []
    };
  },
	submitVote: function(e){
		console.log(e.target.id);
		var payload = {"votes": parseInt(e.target.id)};
		$.ajax({
      type: "POST",
      url: "/api/vote",
      dataType: 'json',
      contentType: 'application/json;charset=utf-8',
      data: JSON.stringify(payload),
      cache: false,
      success: function(data) {
        this.props.onSubmitHandle();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error("/api/vote", status, err.toString());
      }.bind(this)
    });
	},
	dayOfTheWeek: function(){
		return moment.format("dddd");
	},
	render: function(){
		var listItem = this.props.data.map((restaurant) => {
			return(
				<button type="button" className="list-group-item"  id={restaurant.rest_id} onClick={this.submitVote}>
					<span className="badge">{restaurant.votes}</span>
					{restaurant.name}
				</button>
			);
		});

		return(
			<div className={this.props.type}>
				<h3>{this.props.type}</h3>
				<div className="list-group">
					{listItem}
				</div>
			</div>
		);
	}
});

ReactDOM.render(<LunchSelector />, document.getElementById('app'));
