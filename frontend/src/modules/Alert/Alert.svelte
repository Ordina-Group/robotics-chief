<script lang="ts">
  import { Alert as FBAlert } from "flowbite-svelte";
  import { onMount } from "svelte";

  import { currentAlert } from "$lib/alert";

  export let color: 'green' | 'red';
  export let message: String;

  onMount(() => {
    window.setTimeout(() => {
      currentAlert.set(undefined);
    }, 3500);
  });
</script>

<FBAlert border dismissable class="absolute bottom-2 z-50" color={color}>
    {message}
</FBAlert>
