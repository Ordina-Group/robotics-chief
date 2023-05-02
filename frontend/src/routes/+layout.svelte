<script>
  import "../app.postcss";
</script>

<div class="grid container mx-auto gap-1">
    <slot />
</div>

<style>
  @import url("https://fonts.googleapis.com/css2?family=Bruno+Ace+SC&display=swap");

  :global(*) {
    font-family: monospace;
  }

  :global(h1) {
    font-family: "Bruno Ace SC", Helvetica, sans-serif;
  }
</style>
